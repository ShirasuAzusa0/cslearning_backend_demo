package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_graph.Nodes;
import ben.qihuiadmin.entity.vo.NodeRelVO;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
@Transactional("neo4jTransactionManager")
public interface KGRepository extends Neo4jRepository<Nodes, String> {
    // 返回整个图谱
    @Query("""
    MATCH (n)-[r]->(m)
    RETURN
        n {.name, .info, level: toInteger(n.level), .type1, .type2} as startNode,
        m {.name, .info, level: toInteger(m.level), .type1, .type2} as endNode,
        r {.info, .type} as nodeRelationship
    """)
    List<NodeRelVO> getWholeGraphRelationships();

    // 返回结点总数
    @Query("MATCH (n) RETURN COUNT(n)")
    long countNodes();

    // 返回关系总数
    @Query("MATCH ()-[r]->() RETURN COUNT(r)")
    long countRelationships();

    @Query("""
    UNWIND $nodes AS node
    MERGE (n:TechNode {name: node.name})
    SET n.info = node.info,
        n.level = node.level,
        n.type1 = node.type1,
        n.type2 = node.type2
    """)
    void saveNodes(List<Map<String, Object>> nodes);


    @Query("""
    UNWIND $rels AS rel
    MATCH (a:TechNode {name: rel.start})
    MATCH (b:TechNode {name: rel.end})
    MERGE (a)-[r:RELATION {type: rel.type}]->(b)
    SET r.info = rel.info
    """)
    void saveRelationships(List<Map<String, Object>> rels);
}
