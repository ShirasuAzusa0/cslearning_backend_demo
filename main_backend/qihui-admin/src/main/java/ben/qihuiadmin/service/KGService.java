package ben.qihuiadmin.service;

import ben.qihuiadmin.entity.entity_log.Neo4jLog;
import ben.qihuiadmin.entity.vo.NodeRelVO;
import ben.qihuiadmin.repository.KGRepository;
import ben.qihuiadmin.repository.Neo4jLogRepository;
import ben.qihuiadmin.util.jsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KGService {
    private final KGRepository kgRepository;
    private final Neo4jLogRepository neo4jLogRepository;

    public KGService(KGRepository kgRepository, Neo4jLogRepository neo4jLogRepository) {
        this.kgRepository = kgRepository;
        this.neo4jLogRepository = neo4jLogRepository;
    }

    public List<NodeRelVO> findAllNodeRel() {
        return kgRepository.getWholeGraphRelationships();
    }

    public void updateNodeAndRel(MultipartFile graphFile) throws Exception {
        if (graphFile == null || graphFile.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        // 解析 JSON
        String jsonText = new String(graphFile.getBytes(), StandardCharsets.UTF_8);
        List<NodeRelVO> list = jsonUtil.NodeRelToJSON(jsonText);

        Map<String, Map<String, Object>> nodeMap = new HashMap<>();
        List<Map<String, Object>> relationships = new ArrayList<>();

        for (NodeRelVO vo : list) {

            // 起点
            Map<String, Object> start = new HashMap<>();
            start.put("name", vo.getStartNode().getName());
            start.put("info", vo.getStartNode().getInfo());
            start.put("level", vo.getStartNode().getLevel());
            start.put("type1", vo.getStartNode().getType1());
            start.put("type2", vo.getStartNode().getType2());

            // 终点
            Map<String, Object> end = new HashMap<>();
            end.put("name", vo.getEndNode().getName());
            end.put("info", vo.getEndNode().getInfo());
            end.put("level", vo.getEndNode().getLevel());
            end.put("type1", vo.getEndNode().getType1());
            end.put("type2", vo.getEndNode().getType2());

            nodeMap.put((String) start.get("name"), start);
            nodeMap.put((String) end.get("name"), end);

            // 关系
            Map<String, Object> rel = new HashMap<>();
            rel.put("start", start.get("name"));
            rel.put("end", end.get("name"));
            rel.put("type", vo.getNodeRelationship().getType());
            rel.put("info", vo.getNodeRelationship().getInfo());

            relationships.add(rel);
        }

        List<Map<String, Object>> nodes = new ArrayList<>(nodeMap.values());

        // 写入日志
        Neo4jLog newLog = new Neo4jLog();
        newLog.setDescription("Nodes and relationships updated");
        newLog.setContent(jsonText);
        newLog.setLogDate(LocalDateTime.now());
        neo4jLogRepository.save(newLog);

        // 写入 Neo4j
        kgRepository.saveNodes(nodes);
        kgRepository.saveRelationships(relationships);
    }
}
