package ben.qihuiai.service;

import ben.qihuiai.entity.dto.*;
import ben.qihuiai.entity.entity_chat.Messages;
import ben.qihuiai.entity.entity_chat.Models;
import ben.qihuiai.entity.entity_chat.Sessions;
import ben.qihuiai.entity.entity_chat.roleType;
import ben.qihuiai.entity.entity_kb.*;
import ben.qihuiai.entity.vo.*;
import ben.qihuiai.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okio.BufferedSource;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ben.qihuiai.util.jsonUtil.ReferenceDataTOJSON;

@Service
@Slf4j
public class AiService {

    private final SessionRepository sessionRepository;
    private final ModelRepository modelRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChunkingRulesRepository chunkingRulesRepository;
    private final DocumentRepository documentRepository;
    private final KbRepository kbRepository;
    private final DocKbRepository docKbRepository;
    private final ParagraphRepository paragraphRepository;
    private final QuestionRepository questionRepository;
    private final PqRepository pqRepository;
    private final CrossPublishService crossPublishService;
    private final AiKbSearchService aiKbSearchService;

    // 文档分段的存储结构，存储最终分段结果，包含标题和父链
    private static class ParagraphInfo {
        String content;
        String title;
        List<String> parentChain;
        ParagraphInfo(String content, String title, List<String> parentChain) {
            this.content = content;
            this.title = title;
            this.parentChain = parentChain;
        }
    }

    // 用于 splitByRule 返回，content 为文本，titleFromRule 仅当标题规则时有效
    private static class Segment {
        String content;
        String titleFromRule; // 如果该段是由标题规则产生的，则存储提取的标题文本，否则 null
        Segment(String content, String titleFromRule) {
            this.content = content;
            this.titleFromRule = titleFromRule;
        }
    }

    // 提示词拼接
    private final static String prompt1 = "内容：\n";
    private final static String prompt2 = "\n请总结上面的内容，并根据内容总结生成 5 个问题。\n回答要求：\n- 请只输出问题；\n- 问题中指明知识点；\n- 请将每个问题独立放置到<question></question>标签中，每个问题单独一行";

    // HTTP client & mapper
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public AiService(SessionRepository sessionRepository,
                     ModelRepository modelRepository,
                     MessageRepository messageRepository,
                     UserRepository userRepository,
                     ChunkingRulesRepository chunkingRulesRepository,
                     DocumentRepository documentRepository,
                     KbRepository kbRepository,
                     DocKbRepository docKbRepository,
                     ParagraphRepository paragraphRepository,
                     QuestionRepository questionRepository,
                     PqRepository pqRepository, CrossPublishService crossPublishService, AiKbSearchService aiKbSearchService) {
        this.sessionRepository = sessionRepository;
        this.modelRepository = modelRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chunkingRulesRepository = chunkingRulesRepository;
        this.documentRepository = documentRepository;
        this.kbRepository = kbRepository;
        this.docKbRepository = docKbRepository;
        this.paragraphRepository = paragraphRepository;
        this.questionRepository = questionRepository;
        this.pqRepository = pqRepository;
        this.crossPublishService = crossPublishService;
        this.aiKbSearchService = aiKbSearchService;
    }

    // 流式聊天
    public SseEmitter sendMessageStream(ChatMessageDto dto) {
        // 暂时设置为永不超时
        SseEmitter emitter = new SseEmitter(0L);

        // 查 model
        Models model = modelRepository.findByModelId(
                sessionRepository.getSessionsBySessionId(dto.getSessionId())
                        .getModel().getModelId()
        );
        if (model == null) {
            emitter.completeWithError(new RuntimeException("模型不存在"));
            return emitter;
        }

        // 查 session
        Sessions session = sessionRepository.getSessionsBySessionId(dto.getSessionId());
        if (session == null) {
            emitter.completeWithError(new RuntimeException("会话不存在"));
            return emitter;
        }

        // 构造 message 上下文
        List<Map<String, String>> chatMessages = new ArrayList<>();

        // 循环构建历史消息列表
        List<Messages> history =
                messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getSessionId());

        for (Messages m : history) {
            chatMessages.add(Map.of(
                    "role", m.getRole().name(),
                    "content", m.getContent()
            ));
        }

        String query = dto.getMessage();

        // 跨域关联知识库查询，返回结果
        Map<String, Object> searchResult = aiKbSearchService.search(query, 1);

        // 解析 RAG 内容
        List<List<?>> documents = new ArrayList<>();
        Object docsObj = ((Map<String, Object>) searchResult.get("data")).get("documents");
        if (docsObj instanceof List) {
            documents = (List<List<?>>) docsObj;
        }

        // 保存完整的文档信息供后续引用
        List<Map<String, Object>> references = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            List<?> doc = documents.get(i);
            Map<String, Object> ref = new HashMap<>();
            ref.put("index", i + 1);
            ref.put("metadata", doc.get(1));
            references.add(ref);
        }

        // 构造 RAG 上下文
        StringBuilder ragContext = new StringBuilder();
        ragContext.append("以下是与用户问题相关的参考资料：\n\n");
        if (!documents.isEmpty()) {
            for (int i = 0; i < documents.size(); i++) {
                List<?> doc = documents.get(i);
                String content = (String) doc.get(0);

                ragContext.append("【参考资料").append(i + 1).append("】\n");
                ragContext.append(content).append("\n\n");
            }
        }
        ragContext.append("请基于以上资料回答用户问题。");
        ragContext.append("回答时请在引用内容后添加引用标记，格式为 [数字]，例如 [1][2]。");
        ragContext.append("如果资料不足，可以结合你的知识回答，但不要编造。\n\n");
        ragContext.append("以下是用户提的问题：\n");
        ragContext.append(query);

        // 保存用户消息到 messages 表，role = user
        Messages userMsg = new Messages();
        userMsg.setSession(session);
        userMsg.setContent(query);
        userMsg.setCreatedAt(LocalDateTime.now());
        userMsg.setRole(roleType.user);
        userMsg.setReferenceData(null);
        // 用 save 要比 saveAndFlush 要高效些
        messageRepository.save(userMsg);

        // 把 RAG 插入 messages
        chatMessages.add(Map.of(
                "role", "user",
                "content", ragContext.toString()
        ));

        // 构建模型请求体
        Map<String, Object> req = new HashMap<>();
        req.put("model", model.getModelVersion());
        req.put("messages", chatMessages);
        req.put("max_tokens", model.getMaxTokens());
        req.put("stream", true);

        Request request;
        try {
            String body = mapper.writeValueAsString(req);
            request = new Request.Builder()
                    .url(model.getApiURL())
                    .addHeader("Authorization", "Bearer " + model.getApiKey())
                    .post(RequestBody.create(
                            body, MediaType.parse("application/json")))
                    .build();
        } catch (Exception e) {
            emitter.completeWithError(e);
            return emitter;
        }

        StringBuilder assistantBuffer = new StringBuilder();

        // 火山 DeepSeek 不是标准 SSE，不能用 EventSource
        // 必须使用 OkHttp 普通流式读取
        // 使用 OkHttp 普通 Async 回调 + 逐行解析 data:
        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                emitter.completeWithError(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.body() != null) {
                    try (BufferedSource source = response.body().source()) {

                        // SSE 必须有 text/event-stream，官方应当是这样
                        while (!source.exhausted()) {
                            String line = source.readUtf8Line();

                            // SSE data 行
                            if (line != null && line.startsWith("data:")) {
                                String data = line.substring(5).trim();

                                // 如果是完成标识
                                if ("[DONE]".equals(data)) {
                                    // assistant 完整回答入库
                                    Messages assistantMsg = new Messages();
                                    assistantMsg.setSession(session);
                                    assistantMsg.setRole(roleType.assistant);
                                    assistantMsg.setContent(assistantBuffer.toString());
                                    assistantMsg.setCreatedAt(LocalDateTime.now());
                                    if(!references.isEmpty()) {
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        assistantMsg.setReferenceData(objectMapper.writeValueAsString(references));
                                    }
                                    else
                                        assistantMsg.setReferenceData(null);
                                    messageRepository.saveAndFlush(assistantMsg);

                                    // 更新当前会话最后活跃时间
                                    session.setLastUpdatedAt(LocalDateTime.now());
                                    sessionRepository.save(session);

                                    // 发送引用信息
                                    ObjectMapper mapper = new ObjectMapper();
                                    Map<String, Object> referencesData = new HashMap<>();
                                    referencesData.put("type", "references");
                                    referencesData.put("references", references);

                                    try {
                                        String referencesJson = mapper.writeValueAsString(referencesData);
                                        // 发送引用事件（使用自定义事件名）
                                        emitter.send(SseEmitter.event()
                                                .name("references")
                                                .data(referencesJson));
                                    } catch (Exception e) {
                                        log.error("Failed to send references", e);
                                    }

                                    // 发送完成标识
                                    emitter.send("[DONE]");
                                    emitter.complete();
                                    break;
                                }

                                // 推送 SSE chunk
                                emitter.send(data);
                                System.out.println("SSE send: " + data);

                                JsonNode root = mapper.readTree(data);
                                JsonNode delta =
                                        root.path("choices").get(0)
                                                .path("delta").path("content");
                                if (!delta.isMissingNode()) {
                                    assistantBuffer.append(delta.asText());
                                }
                            }
                        }
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }
            }
        });

        return emitter;
    }

    // 获取大模型列表
    public List<ModelListElementVO> getModels() {
        List<Models> modelsList = modelRepository.findAll();
        return modelsList.stream()
                .map(m -> new ModelListElementVO(
                        m.getModelId(),
                        m.getModelName(),
                        m.getModelVersion()
                ))
                .toList();
    }

    // 获取本地大模型列表
    public List<ModelListElementVO> getLocalModels() {
        List<Models> modelsList = modelRepository.findLocalModels();
        return modelsList.stream()
                .map(m -> new ModelListElementVO(
                        m.getModelId(),
                        m.getModelName(),
                        m.getModelVersion()
                ))
                .toList();
    }

    // 获取大模型列表
    public List<ModelListElementVO> getApiModels() {
        List<Models> modelsList = modelRepository.findApiModels();
        return modelsList.stream()
                .map(m -> new ModelListElementVO(
                        m.getModelId(),
                        m.getModelName(),
                        m.getModelVersion()
                ))
                .toList();
    }

    // 获取全部消息
    public List<MessageListElementVO> getMessageList(int sessionId) {
        List<Messages> messageList = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return messageList.stream()
                .map(msg -> new MessageListElementVO(
                        msg.getMessageId(),
                        msg.getContent(),
                        msg.getRole().name(),
                        msg.getCreatedAt(),
                        ReferenceDataTOJSON(msg.getReferenceData())
                ))
                .toList();
    }

    // 获取会话列表
    public List<SessionListElementVO> getSessionList(long userId) {
        List<Sessions> sessionList = sessionRepository.findAllByUserId(userId);
        return sessionList.stream()
                .map(s -> new SessionListElementVO(
                        s.getSessionId(),
                        s.getSessionName(),
                        s.getModel().getModelName(),
                        s.getCreatedAt(),
                        s.getLastUpdatedAt()
                ))
                .toList();
    }

    // 创建新会话
    public NewSessionVO createNewSession(NewSessionDto dto) {
        Sessions session = new Sessions();
        session.setUser(userRepository.findByUserId(dto.getUserId()));
        session.setSessionName("会话_id_"+LocalDateTime.now());
        session.setCreatedAt(LocalDateTime.now());
        session.setLastUpdatedAt(LocalDateTime.now());
        session.setModel(modelRepository.findByModelName(dto.getModelName()));
        sessionRepository.save(session);

        return new NewSessionVO(
                session.getSessionId(),
                dto.getUserId(),
                session.getSessionName(),
                dto.getModelName(),
                session.getCreatedAt()
        );
    }

    // 删除会话
    public void deleteSession(int sessionId) {
        sessionRepository.deleteById(sessionId);
    }

    // 修改会话
    public void editSession(EditSessionDto dto) {
        Sessions session = sessionRepository.getSessionsBySessionId(dto.getSessionId());
        if (StringUtils.hasText(dto.getSessionName())) session.setSessionName(dto.getSessionName());
        if (StringUtils.hasText(dto.getModelName())) session.setModel(modelRepository.findByModelName(dto.getModelName()));
        session.setLastUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    // 清空消息
    @Transactional("transactionManager")
    public void clearAllMessages(int sessionId) {
        messageRepository.deleteBySession(sessionRepository.getSessionsBySessionId(sessionId));
    }

    // 删除单条消息
    @Transactional("transactionManager")
    public void deleteOneMessage(long messageId) {
        messageRepository.deleteByMessageId(messageId);
    }

    // 获取文档分段规则列表
    public List<RuleElementVO> getRules() {
        List<ChunkingRules> ruleList = chunkingRulesRepository.findAllRules();
        return ruleList.stream()
                .map(cr -> new RuleElementVO(
                        cr.getRuleId(),
                        cr.getRuleName(),
                        cr.getDescription()
                ))
                .toList();
    }

    // 获取文档详细信息
    public DocumentVO getDocDetails(long documentId) {
        Documents doc = documentRepository.findDocDetails(documentId);
        return new DocumentVO(
                doc.getDocumentId(),
                doc.getDocumentName(),
                doc.getDocumentSize(),
                doc.getDocumentParts(),
                doc.getDocumentLoadedAt(),
                doc.getDocumentType(),
                doc.getDocumentContent()
        );
    }

    public List<KnowledgeBaseElementVO> getKbList(long userId) {
        List<KnowledgeBases> kbList = kbRepository.findAllByUserId(userId);
        return kbList.stream()
                .map(kb -> new KnowledgeBaseElementVO(
                        kb.getKbId(),
                        kb.getKbName()
                ))
                .toList();
    }

    // 获取知识库详细信息
    public KnowledgeBaseVO getKbDetails(long userId) {
        KnowledgeBases kb = kbRepository.findKbByUserId(userId);
        List<Documents> docList = documentRepository.findDocListByKbId(kb.getKbId());
        return new KnowledgeBaseVO(
                kb.getKbId(),
                kb.getKbName(),
                kb.getKbDescription(),
                kb.getKbDocNum(),
                kb.getKbType(),
                kb.getCreatedAt(),
                kb.getEmbeddingModel().getModelName(),
                kb.getRerankerModel().getModelName(),
                new CreatorVO(
                        kb.getUser().getUserId(),
                        kb.getUser().getUserName()
                ),
                docList.stream()
                        .map(d -> new DocumentProfileVO(
                                d.getDocumentId(),
                                d.getDocumentName(),
                                d.getDocumentSize(),
                                d.getDocumentParts(),
                                d.getDocumentLoadedAt()
                        ))
                        .toList()
        );
    }

    // 创建知识库
    public KnowledgeBaseCreateVO createKnowledgeBase(KbDto dto, long userId) {
        KnowledgeBases kb = new KnowledgeBases();
        kb.setKbName(dto.getKbName());
        kb.setKbDescription(dto.getKbDescription());
        kb.setCreatedAt(LocalDateTime.now());
        kb.setKbType(dto.getKbType());
        kb.setKbDocNum(0);
        kb.setUser(userRepository.findByUserId(userId));
        kb.setEmbeddingModel(modelRepository.findByModelName(dto.getEmbeddingModel()));
        kb.setRerankerModel(modelRepository.findByModelName(dto.getRerankerModel()));
        kbRepository.save(kb);

        return new KnowledgeBaseCreateVO(
                kb.getKbId(),
                kb.getKbName(),
                kb.getUser().getUserName(),
                kb.getKbDescription(),
                kb.getEmbeddingModel().getModelName(),
                kb.getRerankerModel().getModelName(),
                kb.getKbType(),
                kb.getCreatedAt()
        );
    }

    // 工具函数：解析获取文档内容
    private String extractTextFromDocument(MultipartFile document) throws IOException, TikaException {
        Tika tika = new Tika();
        try (InputStream stream = document.getInputStream()) {
            // parseToString 会自动检测文件类型并提取文本
            return tika.parseToString(stream);
        }
    }

    // 工具函数：解析文档类型
    private DocumentTypes getDocumentType(String documentName) {
        if (documentName == null || documentName.isEmpty()) return DocumentTypes.unknown;
        // 提取最后一个点后的内容，即文件的拓展名
        int dotIndex = documentName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == documentName.length() - 1) return DocumentTypes.unknown;
        String extension = documentName.substring(dotIndex + 1).toLowerCase();
        return switch (extension) {
            case "pdf" -> DocumentTypes.pdf;
            case "docx" -> DocumentTypes.docx;
            case "doc" -> DocumentTypes.doc;
            case "markdown", "md" -> DocumentTypes.markdown;
            default -> DocumentTypes.unknown;
        };
    }

    // 工具函数：根据文档类型和内容构建规则序列
    private List<ChunkingRules> buildRuleSequence(String docType, String content, List<ChunkingRules> rulesList) {
        // 将规则按类型分组
        // Map<String, List<ChunkingRules>> rulesByType = rulesList.stream()
        //         .collect(Collectors.groupingBy(ChunkingRules::getRuleType));

        // 按类型划分
        // List<ChunkingRules> markdownRules = rulesByType.getOrDefault("markdown", Collections.emptyList());
        // List<ChunkingRules> otherRules = rulesByType.getOrDefault("other", Collections.emptyList());

        // 按 order 排序，逐个降级
        List<ChunkingRules> sortedRules = rulesList.stream()
                .sorted(Comparator.comparingInt(ChunkingRules::getOrder))
                .toList();

        // 引入要分段的文本，检其中测实际存在的分段级别（正则匹配到才算存在），将不存在的分段级别排除掉
        List<ChunkingRules> existingHeadings = sortedRules.stream()
                .filter(cr -> Pattern.compile(cr.getRegex()).matcher(content).find())
                .toList();

        String lowerType = docType.toLowerCase();
        switch (lowerType) {
            case "markdown":
            case "pdf":
            case "doc":
            case "docx":
                // 添加 otherRules 中的所有规则，按序号逐个降级进行分段
                return new ArrayList<>(existingHeadings);
            default:
                // 未知类型默认只按空行分段
                List<ChunkingRules> defaultSequence = new ArrayList<>();
                defaultSequence.add(rulesList.get(16));
                return defaultSequence;
        }
    }

    // 工具函数：使用单个规则切分文本
    private List<Segment> splitByRule(String content, ChunkingRules rule) {
        List<Segment> result = new ArrayList<>();
        if ("markdown".equals(rule.getRuleType())) {
            // 标题规则：按标题行切分，每个块包含标题行
            Pattern pattern = Pattern.compile(rule.getRegex());
            Matcher matcher = pattern.matcher(content);
            // 上一个标题行的结束位置
            int lastHeadingEnd = -1;
            // 上一个标题行的文本
            String lastTitle = null;
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                if (lastHeadingEnd != -1) {
                    // 上一个标题到当前标题之间的内容（上一个标题的正文）
                    String between = content.substring(lastHeadingEnd, start);
                    if (!between.trim().isEmpty()) {
                        result.add(new Segment(between, lastTitle));
                    }
                } else {
                    // 第一个标题之前的内容
                    if (start > 0) {
                        String leading = content.substring(0, start);
                        if (!leading.trim().isEmpty()) {
                            result.add(new Segment(leading, null));
                        }
                    }
                }
                // 提取当前标题文本
                String headingLine = content.substring(start, end);
                lastTitle = headingLine.replaceAll("^#+\\s+", "").trim();
                lastHeadingEnd = end;
            }
            // 最后一个标题之后的内容
            if (lastHeadingEnd != -1) {
                if (lastHeadingEnd < content.length()) {
                    String remaining = content.substring(lastHeadingEnd);
                    if (!remaining.trim().isEmpty()) {
                        result.add(new Segment(remaining, lastTitle));
                    }
                }
            } else {
                // 没有标题，整个内容作为一段
                result.add(new Segment(content, null));
            }
        } else {
            // 其他规则保持不变
            String[] parts = content.split(rule.getRegex());
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    result.add(new Segment(part, null));
                }
            }
            if (result.isEmpty()) result.add(new Segment(content, null));
        }
        return result;
    }

    // 判断文本中是否可能包含某规则的匹配
    private boolean mayContainRule(String content, ChunkingRules rule) {
        return Pattern.compile(rule.getRegex()).matcher(content).find();
    }

    // 工具函数：递归实现文本分段，content 为待分段文本，ruleSequence 为规则序列，index 为当前使用规则的索引
    private List<ParagraphInfo> recursiveSplit(String content, List<ChunkingRules> ruleSequence, int index, String currentTitle, List<String> parentChain) {
        if (index >= ruleSequence.size()) {
            // 若没有更多规则，将当前内容作为一个段落
            List<String> fullPath = new ArrayList<>(parentChain);
            if (currentTitle != null && !currentTitle.trim().isEmpty()) {
                fullPath.add(currentTitle);
            }
            return Collections.singletonList(new ParagraphInfo(content, currentTitle, fullPath));
        }

        ChunkingRules currentRule = ruleSequence.get(index);
        List<Segment> segments = splitByRule(content, currentRule);

        // 若分段后只有一段，则说明该规则不适用，尝试下一个规则
        if (segments.size() <= 1 && segments.get(0).titleFromRule == null)
            return recursiveSplit(content, ruleSequence, index + 1, currentTitle, parentChain);

        List<ParagraphInfo> result = new ArrayList<>();
        for (Segment seg : segments) {
            // 计算新标题和父标题链
            String newTitle;
            List<String> newParentChain;
            if (seg.titleFromRule != null) {
                // 遇到标题规则块，则新标题为 seg.titleFromRule，父链为原父链 + 原当前标题（若存在）
                newTitle = seg.titleFromRule;
                newParentChain = new ArrayList<>(parentChain);
                if (currentTitle != null) {
                    newParentChain.add(currentTitle);
                }
            } else {
                newTitle = currentTitle;
                newParentChain = new ArrayList<>(parentChain);
            }

            // 若下一级规则存在且当前片段可能含有该规则的标记，则递归分段
            if (index + 1 < ruleSequence.size() && mayContainRule(seg.content, ruleSequence.get(index + 1))) {
                result.addAll(recursiveSplit(seg.content, ruleSequence, index + 1, newTitle, newParentChain));
            } else {
                // 不再递归，构建最终的段落
                List<String> fullPath = new ArrayList<>(newParentChain);
                if (newTitle != null && !newTitle.trim().isEmpty()) {
                    fullPath.add(newTitle);
                }
                result.add(new ParagraphInfo(seg.content, newTitle, fullPath));
            }
        }
        return result;
    }

    // 工具函数：切分长度大于1000的段落（按800个字符为一段强制切分，做兜底处理）
    private List<ParagraphInfo> splitLongParagraphs(List<ParagraphInfo> paragraphs) {
        List<ParagraphInfo> result = new ArrayList<>();
        for (ParagraphInfo p : paragraphs) {
            if (p.content.length() > 1000) {
                String content = p.content;
                int len = content.length();
                int start = 0;
                while (start < len) {
                    int end = Math.min(start + 800, len);
                    String subContent = content.substring(start, end);
                    result.add(new ParagraphInfo(subContent, p.title, new ArrayList<>(p.parentChain)));
                    start = end;
                }
            } else {
                result.add(p);
            }
        }
        return result;
    }

    // 工具函数：合并相邻且标题相同的短段落（长度小于100个字符），并确保合并后总长度不超过1000个字符
    private List<ParagraphInfo> mergeShortParagraphs(List<ParagraphInfo> paragraphs) {
        if(paragraphs.isEmpty()) return paragraphs;
        List<ParagraphInfo> result = new ArrayList<>();
        ParagraphInfo current = paragraphs.get(0);
        for (int i = 1; i < paragraphs.size(); i++) {
            ParagraphInfo next = paragraphs.get(i);
            // 条件：当前段短，标题相同，且合并后不超过1000个字符
            if (current.content.length() < 100
                    && Objects.equals(current.title, next.title)
                    && current.content.length() + next.content.length() <= 1000) {
                // 合并
                String mergedContent = current.content + next.content;
                current = new ParagraphInfo(mergedContent, current.title, current.parentChain);
            } else {
                result.add(current);
                current = next;
            }
        }
        result.add(current);
        return result;
    }

    // 工具函数：后处理，先切分长段，再合并短段
    private List<ParagraphInfo> postProcessParagraphs(List<ParagraphInfo> paragraphs) {
        List<ParagraphInfo> result = splitLongParagraphs(paragraphs);
        result = mergeShortParagraphs(result);
        return result;
    }

    // 工具函数：文档智能分段，返回分段列表
    private List<ParagraphInfo> documentAutoSplit(String documentType, String content) {
        List<ParagraphInfo> paragraphs;

        // 获取所有分段规则
        List<ChunkingRules> ruleList = chunkingRulesRepository.findAllRules();

        // 根据文档类型构建规则序列
        List<ChunkingRules> ruleSequence = buildRuleSequence(documentType, content, ruleList);

        // 递归分段
        paragraphs = recursiveSplit(content, ruleSequence, 0, null, new ArrayList<>());

        // 分段长度后处理
        paragraphs = postProcessParagraphs(paragraphs);

        // 返回分段列表
        return paragraphs;
    }

    private List<String> generateQuestionByParagraphs(int modelId, ParagraphInfo paragraph) throws Exception {
        List<String> result = new ArrayList<>();
        String prompt;
        prompt = prompt1 +
                "标题：" + paragraph.title + "\n" +
                "递进关系：" + paragraph.parentChain + "\n" +
                "详情：" + paragraph.content + "\n" +
                prompt2;

        Models model = modelRepository.findByModelId(modelId);
        Map<String, Object> req = new HashMap<>();
        req.put("model", model.getModelVersion());
        req.put("messages", List.of(
                Map.of(
                        "role", "user",
                        "content", prompt
                )
        ));
        req.put("max_tokens", 100);
        req.put("stream", false);
        req.put("temperature", 0);

        Request request = new Request.Builder()
                .url(model.getApiURL())
                .addHeader("Authorization", "Bearer " + model.getApiKey())
                .post(RequestBody.create(
                        mapper.writeValueAsString(req),
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return result;
            }

            String body = response.body().string();

            // 解析大模型返回
            String content = mapper.readTree(body)
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

            // 兜底处理
            if (content.isEmpty()) {
                return result;
            }

            // 对大模型返回的关联问题数据进行清洗
            // 按换行拆分
            String[] questionList = content.split("\n");
            for (String q : questionList) {
                if (!q.isEmpty()) {
                    // 剔除序号等无关内容
                    q = q.replaceAll("^\\d+\\.\\s*", "");
                    // 匹配内容
                    Pattern pattern = Pattern.compile("<question>(.*?)</question>", Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(q);
                    if (matcher.find()) {
                        result.add(matcher.group(1));
                    }
                }
            }
        }
        return result;
    }

    // 上传文档
    public List<DocumentProfileVO> uploadDoc(int kbId, int modelId, List<MultipartFile> documents) throws Exception {
        List<DocumentProfileVO> result = new ArrayList<>();
        if (documents != null) {
            for (MultipartFile document : documents) {
                // 获取文档类型
                String docTypes = getDocumentType(document.getOriginalFilename()).toString();

                // 解析获取文档内容
                String content;
                try {
                    content = extractTextFromDocument(document);
                } catch (Exception e) {
                    throw new RuntimeException("文件文本提取失败: " + document.getOriginalFilename(), e);
                }

                // 根据类型进行智能分段
                List<ParagraphInfo> docParts = documentAutoSplit(docTypes, content);

                // 存储文档基本信息到数据库中
                Documents doc = new Documents();
                doc.setDocumentName(document.getOriginalFilename());
                doc.setDocumentParts(docParts.size());
                doc.setDocumentSize(document.getSize());
                doc.setDocumentLoadedAt(LocalDateTime.now());
                doc.setDocumentType(docTypes);
                doc.setDocumentContent(content);
                documentRepository.save(doc);

                // 建立文档与知识库的关系并存储
                doc_kb dk = new doc_kb();
                dk.setKb(kbRepository.findByKbId(kbId));
                dk.setDocument(doc);
                docKbRepository.save(dk);

                // 存储分段
                int order = 0;
                String title;
                StringBuilder chain = new StringBuilder();
                for (ParagraphInfo part : docParts) {
                    order++;
                    Paragraphs paragraph = new Paragraphs();
                    paragraph.setContent(part.content);
                    title = part.title;
                    if (title == null || title.trim().isEmpty()) {
                        // 自动截取内容前20个字符作为标题
                        title = part.content.length() > 20 ? part.content.substring(0, 20) + "..." : part.content;
                    }
                    paragraph.setTitle(title);
                    paragraph.setParagraphOrder(order);
                    paragraph.setSplitAt(LocalDateTime.now());
                    for (String chainPart : part.parentChain) {
                        if (chainPart.equals(part.parentChain.get(0))) {
                            chain = new StringBuilder(chainPart);
                        } else {
                            chain.append(" > ").append(chainPart);
                        }
                    }
                    paragraph.setParentChain(chain.toString());
                    paragraph.setContext("【递进关系】 " + paragraph.getParentChain() + "\n【标题】 " + title + "\n【内容】 " + part.content);
                    paragraph.setDocument(documentRepository.findByDocumentId(doc.getDocumentId()));
                    paragraph.setKb(kbRepository.findByKbId(kbId));
                    paragraphRepository.save(paragraph);

                    // 调用大模型对分段生成关联问题并存储
                    List<String> questionList = generateQuestionByParagraphs(modelId, part);
                    for (String q : questionList) {
                        Questions question = new Questions();
                        question.setContent(q);
                        question.setKb(kbRepository.findByKbId(kbId));
                        questionRepository.save(question);

                        // 建立分段与关联问题的关系并存储
                        paragraph_questions pq = new paragraph_questions();
                        pq.setParagraph(paragraph);
                        pq.setQuestion(question);
                        pq.setKb(kbRepository.findByKbId(kbId));
                        pq.setDocument(documentRepository.findByDocumentId(doc.getDocumentId()));
                        pqRepository.save(pq);
                    }
                }

                if (docParts.size() > 1) {
                    result.add(new DocumentProfileVO(
                            doc.getDocumentId(),
                            doc.getDocumentName(),
                            doc.getDocumentSize(),
                            doc.getDocumentParts(),
                            doc.getDocumentLoadedAt()
                    ));
                }
            }

            // 跨后端传输数据进行向量化处理
            vectorize(kbId);
        }

        return result;
    }

    // 数据向量化
    public void vectorize(int kbId) {
        List<VectorizationDto> dtos = new ArrayList<>();
        List<Paragraphs> paragraphList = paragraphRepository.findByKbId(kbId);
        for (Paragraphs paragraph : paragraphList) {
            // parentChain + title + content
            StringBuilder contextEmbed = new StringBuilder(paragraph.getContext() + "\n【相关问题】\n");
            List<Questions> questionsList = questionRepository.findByParagraphs(paragraph.getParagraphId());
            for(Questions question : questionsList) {
                // 关联问题
                contextEmbed.append("• ").append(question.getContent()).append("\n");
            }

            VectorizationDto dto = new VectorizationDto();
            dto.setKbId(kbId);
            dto.setDocumentId(paragraph.getDocument().getDocumentId());
            dto.setContext(contextEmbed.toString());

            dtos.add(dto);
        }

        // 调用 embedding 模型进行向量化并存入 chroma 向量数据库中
        crossPublishService.publishVectorizationTasks(dtos);
    }

    // 删除知识库（实际上是删除文档与知识库的联系）
    @Transactional("transactionManager")
    public void deleteKnowledgeBase(int kbId) {
        List<Documents> docList = documentRepository.findDocListByKbId(kbId);
        if (docList != null) {
            // 删除归属于该知识库的文档（仅删除关系）
            docKbRepository.deleteByKb(kbRepository.findByKbId(kbId));
            // 删除归属于该知识库的分段（仅删除关系）
            paragraphRepository.deleteByKb(kbRepository.findByKbId(kbId));
            // 处理文档，若该文档已不归属于任何一个知识库则将文档也删除
            for (Documents doc : docList) {
                if (docKbRepository.findByDocumentId(doc.getDocumentId()) == null) {
                    documentRepository.deleteByDocumentId(doc.getDocumentId());
                }
            }
        }
        // 最后删除知识库
        kbRepository.deleteById(kbId);
    }

    // 删除文档
    @Transactional("transactionManager")
    public void deleteDocuments(DocumentDeleteDto dto) {
        for (long id : dto.getDocumentIds()) {
            docKbRepository.deleteByDocumentAndKb(id, dto.getKbId());
            paragraphRepository.deleteByDocumentAndKb(id, dto.getKbId());
        }
        // 处理文档，若该文档已不归属于任何一个知识库则将文档也删除
        for (long id : dto.getDocumentIds()) {
            if (docKbRepository.findByDocumentId(id) == null) {
                documentRepository.deleteByDocumentId(id);
            }
        }
    }
}
