package ben.qihuiai.controller;

import ben.qihuiai.entity.RestBean;
import ben.qihuiai.entity.dto.*;
import ben.qihuiai.entity.vo.*;
import ben.qihuiai.service.AiService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    // 发送信息
    @PostMapping(value = "/sendMessage", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessage(@RequestBody ChatMessageDto dto) {
        return aiService.sendMessageStream(dto);
    }

    // 获取所有可选的大模型
    @GetMapping("/model/list")
    public ResponseEntity<?> getModelList() {
        List<ModelListElementVO> vos = aiService.getModels();
        return ResponseEntity.ok(RestBean.successType1("获取模型列表成功", vos));
    }

    // 获取本地部署的可选的大模型
    @GetMapping("/model/local")
    public ResponseEntity<?> getLocalModel() {
        List<ModelListElementVO> vos = aiService.getLocalModels();
        return ResponseEntity.ok(RestBean.successType1("获取模型列表成功", vos));
    }

    // 获取通过api调用的可选的大模型
    @GetMapping("/model/api")
    public ResponseEntity<?> getApiModel() {
        List<ModelListElementVO> vos = aiService.getApiModels();
        return ResponseEntity.ok(RestBean.successType1("获取模型列表成功", vos));
    }

    // 获取当前用户指定会话下的所有消息
    @GetMapping("/session/{sessionId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable(name = "sessionId") int sessionId) {
        List<MessageListElementVO> vos = aiService.getMessageList(sessionId);
        return ResponseEntity.ok(RestBean.successType1("获取当前用户指定会话下的所有信息成功", vos));
    }

    // 获取当前用户的所有聊天会话
    @GetMapping("/session/list")
    public ResponseEntity<?> getSessionList(@RequestParam(name = "userId") int userId) {
        List<SessionListElementVO> vos = aiService.getSessionList(userId);
        return ResponseEntity.ok(RestBean.successType1("获取当前用户的所有聊天会话成功", vos));
    }

    // 创建新的聊天会话
    @PostMapping("/session/create")
    public ResponseEntity<?> newSession(@RequestBody NewSessionDto dto) {
        NewSessionVO vo = aiService.createNewSession(dto);
        return ResponseEntity.ok(RestBean.successType1("创建新的聊天会话成功", vo));
    }

    // 删除聊天会话
    @DeleteMapping("/session/delete")
    public ResponseEntity<?> deleteSession(@RequestParam(name = "sessionId") int sessionId) {
        aiService.clearAllMessages(sessionId);
        aiService.deleteSession(sessionId);
        return ResponseEntity.ok(RestBean.successType3("删除聊天会话成功", sessionId));
    }

    // 修改聊天会话
    @PutMapping("/session/update")
    public ResponseEntity<?> updateSession(@RequestBody EditSessionDto dto) {
        aiService.editSession(dto);
        return ResponseEntity.ok(RestBean.successType3("修改聊天会话成功", dto.getSessionId()));
    }

    // 清空会话历史
    @DeleteMapping("/message/clear/{sessionId}")
    public ResponseEntity<?> deleteAllMessage(@PathVariable(name = "sessionId") int sessionId) {
        aiService.clearAllMessages(sessionId);
        return ResponseEntity.ok(RestBean.successType3("清空会话历史成功", sessionId));
    }

    // 删除单条消息
    @DeleteMapping("/message/delete/{messageId}")
    public ResponseEntity<?> deleteOneMessage(@PathVariable(name = "messageId") int messageId) {
        aiService.deleteOneMessage(messageId);
        return ResponseEntity.ok(RestBean.successType4("删除单条信息成功", messageId));
    }

    // 获取文档分段规则列表
    @GetMapping("/knowledgebase/chunkingrules")
    public ResponseEntity<?> getKnowledgeBaseChunkingRules() {
        List<RuleElementVO> vos = aiService.getRules();
        return ResponseEntity.ok(RestBean.successType1("获取文档分段规则列表成功", vos));
    }

    // 获取文档详细信息
    @GetMapping("/knowledgebase/document/{documentId}")
    public ResponseEntity<?> getDocumentDetails(@PathVariable(name = "documentId") long documentId) {
        DocumentVO vo = aiService.getDocDetails(documentId);
        return ResponseEntity.ok(RestBean.successType1("获取文档详细信息成功", vo));
    }

    // 获取知识库概要信息
    @GetMapping("/knowledgebase/list/{userId}")
    public ResponseEntity<?> getKnowledgeBaseList(@PathVariable(name = "userId") long userId) {
        List<KnowledgeBaseElementVO> vos = aiService.getKbList(userId);
        return ResponseEntity.ok(RestBean.successType1("获取知识库概要信息成功", vos));
    }

    // 获取知识库详细信息
    @GetMapping("/knowledgebase/detail/{userId}")
    public ResponseEntity<?> getKnowledgeBaseDetail(@PathVariable(name = "userId") long userId) {
        KnowledgeBaseVO vo = aiService.getKbDetails(userId);
        return ResponseEntity.ok(RestBean.successType1("获取知识库详细信息成功", vo));
    }

    // 创建知识库
    @PostMapping("/knowledgebase/create")
    public ResponseEntity<?> createKnowledgeBase(@RequestBody KbDto dto, @RequestParam(name = "userId") long userId) {
        KnowledgeBaseCreateVO vo = aiService.createKnowledgeBase(dto, userId);
        return ResponseEntity.ok(RestBean.successType1("创建知识库成功", vo));
    }

    // 上传文档
    // 通过 consumes = MediaType.MULTIPART_FORM_DATA_VALUE 告知 Spring 本接口只接受 multipart/form-data 请求
    @PostMapping("/knowledgebase/documents/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam(required = false, name = "kbId") int kbId,
                                            @RequestParam(required = false, name = "modelId") int modelId,
                                            @RequestPart(required = false, name = "documents") List<MultipartFile> documents
    ) throws Exception {
        List<DocumentProfileVO> vos = aiService.uploadDoc(kbId, modelId, documents);
        return ResponseEntity.ok(RestBean.successType1("上传文档成功", vos));
    }

    // 删除知识库
    @DeleteMapping("/knowledgebase/{kbId}/delete")
    public ResponseEntity<?> deleteKnowledgeBase(@PathVariable(name = "kbId") int kbId) {
        aiService.deleteKnowledgeBase(kbId);
        return ResponseEntity.ok(RestBean.successType5("删除知识库成功", kbId));
    }

    // 删除文档
    @DeleteMapping("/knowledgebase/documents/delete")
    public ResponseEntity<?> deleteDocuments(@RequestBody DocumentDeleteDto dto) {
        aiService.deleteDocuments(dto);
        return ResponseEntity.ok(RestBean.successType6("删除文档成功", dto.getDocumentIds()));
    }
}
