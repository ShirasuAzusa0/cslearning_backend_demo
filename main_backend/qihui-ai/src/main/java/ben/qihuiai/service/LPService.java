package ben.qihuiai.service;

import ben.qihuiai.entity.dto.LearningInfoDto;
import ben.qihuiai.entity.dto.QuizResultDto;
import ben.qihuiai.entity.dto.QuizResultListDto;
import ben.qihuiai.entity.entity_chat.Models;
import ben.qihuiai.entity.entity_user.Users;
import ben.qihuiai.entity.vo.*;
import ben.qihuiai.repository.GraphRepository;
import ben.qihuiai.repository.ModelRepository;
import ben.qihuiai.repository.UserRepository;
import ben.qihuiai.util.BKTParam;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static ben.qihuiai.util.jsonUtil.NodeRelToJSON;
import static ben.qihuiai.util.promptUtil.getLearningPathOptimizationPrompt;
import static ben.qihuiai.util.promptUtil.getQuizPrompt;

@Service
public class LPService {
    private final ModelRepository modelRepository;
    Map<Integer, String> levelMap = Map.of(
            1, "入门",
            2, "初级",
            3, "进阶",
            4, "深入"
    );

    // HTTP client & mapper
    private final ObjectMapper mapper = new ObjectMapper();

    private final GraphRepository graphRepository;
    private final UserRepository userRepository;

    public LPService(UserRepository userRepository, GraphRepository graphRepository, ModelRepository modelRepository) {
        this.userRepository = userRepository;
        this.graphRepository = graphRepository;
        this.modelRepository = modelRepository;
    }

    // 判断用户是否进行过学习路线定制——new表示没有，old表示有
    public String getUserGraphAndBKT(long userId) {
        String type;
        Users user = userRepository.findByUserId(userId);
        if (user.getLearningPath() == null) {
            type = "new";
        } else {
            type = "old";
        }
        return type;
    }

    // 生成定制化学习路径
    public LearningPathVO generateLearningPath(QuizResultListDto dto) {
        // 获取用户当前的表
        Users user = userRepository.findByUserId(dto.getUserId());
        List<NodeRelVO> learnPath = JSON.parseArray(user.getLearningPath(), NodeRelVO.class);
        Map<String, Map<String, Double>> BKTtable = JSON.parseObject(
                user.getBktTable(),
                new TypeReference<>() {
                });

        // 获取测试后的数据，查表，计算并修改其值
        for (QuizResultDto qrDto : dto.getQuizResults()) {
            String kp = qrDto.getKnowledge_point();
            double accuracy = qrDto.getCorrect_rate();
            BKTParam param = buildFullBKTParamTable().get(kp);
            if (param == null) continue;
            for (Map<String, Double> levelMap : BKTtable.values()) {
                if (levelMap.containsKey(kp)) {
                    double prior = levelMap.get(kp);

                    // 首次测试初始化
                    if (prior == 0.0) {
                        prior = param.getP0();
                    }
                    double updated = BKTGetUpdated(param, accuracy, prior);
                    levelMap.put(kp, updated);
                    break;
                }
            }
        }
        // 获取已掌握的节点集合（BKT >= 0.85）
        Set<String> masteredNodes = new HashSet<>();
        for (Map<String, Double> levelMap : BKTtable.values()) {
            for (Map.Entry<String, Double> entry : levelMap.entrySet()) {
                if (entry.getValue() >= 0.85) {
                    masteredNodes.add(entry.getKey());
                }
            }
        }

        // BKT结果表存回数据库
        user.setBktTable(JSON.toJSONString(BKTtable));

        // 学习路线剪支并做最后的优化
        String prompt = buildPrompt2(learnPath, masteredNodes, dto.getTarget(), dto.getTend());

        // 调用大模型，并清洗返回数据
        String llmResponse;
        try {
            llmResponse = generateByLLM(prompt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 解析模型返回
        LearningPathVO vo = JSON.parseObject(llmResponse, LearningPathVO.class);
        user.setLearningPath(JSON.toJSONString(vo.getFinalPath()));
        user.setLearningPathDescription(vo.getExplanation());
        userRepository.save(user);
        return vo;
    }

    private static double BKTGetUpdated(BKTParam param, double accuracy, double prior) {
        double guess = param.getGuess();
        double slip = param.getSlip();
        double learn = param.getLearn();

        double posterior;

        // 答对
        if (accuracy >= 0.6) {
            posterior = (prior * (1 - slip)) / (prior * (1 - slip) + (1 - prior) * guess);
        }
        // 答错
        else {
            posterior = (prior * slip) / (prior * slip + (1 - prior) * (1 - guess));
        }

        double updated = posterior + (1 - posterior) * learn;

        updated = Math.max(0.0, Math.min(1.0, updated));
        return updated;
    }

    // 获取已有的定制化学习路径
    public LearningPathVO gainLearningPath(long userId) {
        Users user = userRepository.findByUserId(userId);
        return new LearningPathVO(
                NodeRelToJSON(user.getLearningPath()),
                user.getLearningPathDescription()
        );
    }

    // 获取可选的学习目标列表
    public cltVO getLearningTarget() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource("classpath:targetList.json");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(resource.getInputStream(), cltVO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 获取测试题目
    public List<QuizVO> getQuiz(LearningInfoDto dto) {
        int startLevel;
        NodeVO vo = graphRepository.getNodeProfile(dto.getLearningTarget());
        if (dto.getLearningStage() > vo.getLevel()) {
            startLevel = vo.getLevel() - 1;
        }
        else {
            startLevel = dto.getLearningStage();
        }
        int endLevel = vo.getLevel();
        List<NodeRelVO> vos = graphRepository.getNodeRelByLevel(
                Integer.toString(startLevel), Integer.toString(endLevel), vo.getName());

        // 转换成 JSON 格式暂存并发送给大模型
        String knowledgeGraphJson = JSON.toJSONString(vos);
        Users user = userRepository.findByUserId(dto.getUserId());
        user.setLearningPath(knowledgeGraphJson);
        userRepository.save(user);

        // 生成 prompt
        String prompt = buildPrompt1(dto, vo, knowledgeGraphJson);

        // 调用大模型，并清洗返回数据
        String llmResponse;
        try {
            llmResponse = generateByLLM(prompt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 解析模型返回
        return JSON.parseArray(llmResponse, QuizVO.class);
    }

    private String buildPrompt1(
            LearningInfoDto dto,
            NodeVO vo,
            String knowledgeGraphJson
    ) {

        return getQuizPrompt().formatted(
                dto.getLearningTarget(),
                levelMap.get(dto.getLearningStage()),
                levelMap.get(vo.getLevel()),
                dto.getAvailableTime(),
                knowledgeGraphJson
        );
    }

    private String buildPrompt2(
            List<NodeRelVO> vos,
            Set<String> masteredNodes,
            String target,
            String tend
    ) {
        String nodeRelJson = JSON.toJSONString(vos);
        String masteredJson = JSON.toJSONString(masteredNodes);
        return getLearningPathOptimizationPrompt().formatted(
                nodeRelJson, masteredJson, target, tend
        );
    }

    private String generateByLLM(String prompt) throws Exception {
        Models model = modelRepository.findByModelId(1);

        // 构造请求体
        Map<String, Object> req = new HashMap<>();
        req.put("model", model.getModelVersion());
        req.put("messages", List.of(
                Map.of(
                        "role", "user",
                        "content", prompt
                )
        ));
        req.put("max_tokens", 8000);   // 适当拉高最大 token 值
        req.put("stream", false);
        req.put("temperature", 0.7);   // 适当创造性

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(model.getApiURL())
                .addHeader("Authorization", "Bearer " + model.getApiKey())
                .post(RequestBody.create(
                        mapper.writeValueAsString(req),
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("LLM request failed");
            }

            String body = response.body().string();

            String content = mapper.readTree(body)
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

            if (content.isEmpty()) {
                throw new RuntimeException("LLM returned empty result");
            }

            content = content
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            return content;
        }
    }

    public Map<String, BKTParam> buildFullBKTParamTable() {

        Map<String, BKTParam> table = new HashMap<>();

        // ====== Level 1 ======
        BKTParam level1 = new BKTParam(0.30, 0.20, 0.25, 0.10);
        String[] level1Kp = {
                "Java","Python","Go","Node.js","Rust",
                "HTML5","CSS3","Java Script ES6","ESLint和Prettier"
        };
        for(String kp : level1Kp) table.put(kp, level1);

        // ====== Level 2 ======
        BKTParam level2 = new BKTParam(0.20, 0.15, 0.20, 0.10);
        String[] level2Kp = {
                "RESTful API","Controller-Service-DAO","Spring Boot","Django","Flask",
                "Gin","FastAPI","Echo + Fiber","ASP.NET Core","Express.js",
                "Actix","Rocket","MySQL","MongoDB","Redis","SQLite","InfluxDB","Neo4j",
                "TypeScript","React","Vue3","Angular",
                "Vite前端","Webpack","Rollup","npm","yarn","pnpm"
        };
        for(String kp : level2Kp) table.put(kp, level2);

        // ====== Level 3 ======
        BKTParam level3 = new BKTParam(0.10, 0.10, 0.15, 0.08);
        String[] level3Kp = {
                "GraphQL","gRPC","WebSocket","Kafka","RabbitMQ","NATS",
                "Docker","K8s","Azure Functions","AWS Lambda","Istio",
                "Elasticsearch","Amazon Aurora",
                "Redux","Pinia","Zustand","RxJS",
                "Tailwind CSS","Element Plus","Next.js","Nuxt.js","Astro"
        };
        for(String kp : level3Kp) table.put(kp, level3);

        // ====== Level 4 ======
        BKTParam level4 = new BKTParam(0.05, 0.08, 0.10, 0.05);
        String[] level4Kp = {
                "Spring Cloud","CQRS和Event Sourcing","Apache Spark",
                "qiankun","Module Federation","WebAssembly"
        };
        for(String kp : level4Kp) table.put(kp, level4);

        return table;
    }
}
