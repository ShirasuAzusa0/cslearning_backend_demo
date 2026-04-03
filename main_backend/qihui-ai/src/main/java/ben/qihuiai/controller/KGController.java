package ben.qihuiai.controller;

import ben.qihuiai.entity.RestBean;
import ben.qihuiai.entity.vo.MultiResourceVO;
import ben.qihuiai.entity.vo.NodeRelVO;
import ben.qihuiai.service.KGService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learn")
public class KGController {
    private final KGService kgService;
    public KGController(KGService kgService) {
        this.kgService = kgService;
    }

    // 获取节点多模态学习资源（文档+视频）
    @GetMapping("/knowledge_graph/resources")
    public ResponseEntity<?> getKnowledgeGraphNodeLearnResources(@RequestParam(name = "nodeName") String nodeName) {
        MultiResourceVO vo = kgService.getMultiLearningResources(nodeName);
        return ResponseEntity.ok(RestBean.successType1("获取多模态学习资源成功", vo));
    }

    // 获取知识图谱所有节点与关系
    @GetMapping("/knowledge_graph")
    public ResponseEntity<?> getKnowledgeGraphAllNodes() {
        List<NodeRelVO> vos = kgService.getAllNodes();
        return ResponseEntity.ok(RestBean.successType1("获取知识图谱所有节点与关系成功", vos));
    }

    // 获得知识图谱指定节点下分的关系
    @GetMapping("/knowledge_graph/category")
    public ResponseEntity<?> getNextLevelRelationship(@RequestParam(name = "nodeName") String nodeName) {
        List<NodeRelVO> vos = kgService.getNextLevelNodes(nodeName);
        return ResponseEntity.ok(RestBean.successType1("获得知识图谱指定节点下分的关系成功", vos));
    }

    // 获取指定节点的同级关系
    @GetMapping("/knowledge_graph/same_level_relationship")
    public ResponseEntity<?> getSameLevelRelationship(@RequestParam(name = "nodeName") String nodeName) {
        List<NodeRelVO> vos = kgService.getSameLevelNodes(nodeName);
        return ResponseEntity.ok(RestBean.successType1("获取指定节点的同级关系成功", vos));
    }
}
