package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_graph.Nodes;
import ben.qihuiai.entity.vo.NodeRelVO;
import ben.qihuiai.entity.vo.NodeVO;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional("neo4jTransactionManager")
public interface GraphRepository extends Neo4jRepository<Nodes, String> {

    // 返回整个图谱
    @Query("""
    MATCH (n)-[r]->(m)
    RETURN
        n {.name, .info, level: toInteger(n.level), .type1, .type2} as startNode,
        m {.name, .info, level: toInteger(m.level), .type1, .type2} as endNode,
        r {.info, .type} as nodeRelationship
    """)
    List<NodeRelVO> getWholeGraphRelationships();

    // 返回同级关系（所有同 level 的节点，以及它们之间的边）
    @Query("""
    MATCH (a)
    WHERE a.level = $level
    OPTIONAL MATCH (a)-[r]->(b)
    WHERE b.level = $level
    RETURN
        {
            name: a.name,
            info: a.info,
            level: toInteger(a.level),
            type1: a.type1,
            type2: a.type2
        } as startNode,
        COALESCE(
            {
                name: b.name,
                info: b.info,
                level: toInteger(b.level),
                type1: b.type1,
                type2: b.type2
            },
            null
        ) as endNode,
        COALESCE(
            {
                info: r.info,
                type: r.type
            },
            null
        ) as nodeRelationship
    """)
    List<NodeRelVO> getSameLevelRelationships(@Param("level") String level);


    // 返回向下延伸关系
    @Query("""
    MATCH path = (start {name: $name})-[*]->(leaf)
    WHERE NOT (leaf)-[]->()
    WITH relationships(path) AS rels, nodes(path) AS nodes
    UNWIND range(0, size(rels)-1) AS i
    RETURN DISTINCT
        {
            name: nodes[i].name,
            info: nodes[i].info,
            level: toInteger(nodes[i].level),
            type1: nodes[i].type1,
            type2: nodes[i].type2
        } as startNode,
        {
            name: nodes[i+1].name,
            info: nodes[i+1].info,
            level: toInteger(nodes[i+1].level),
            type1: nodes[i+1].type1,
            type2: nodes[i+1].type2
        } as endNode,
        {
            info: rels[i].info,
            type: rels[i].type
        } as nodeRelationship
    """)
    List<NodeRelVO> getAllDescendantRelationships(@Param("name") String name);

    // 获取节点参数内容
    @Query("""
    MATCH (n {name: $nodeName})
    RETURN
        {
            name: n.name,
            info: n.info,
            level: toInteger(n.level),
            type1: n.type1,
            type2: n.type2
        } as node
    """)
    NodeVO getNodeProfile(@Param("nodeName") String nodeName);

    // 返回原始学习路线
    @Query("""
    MATCH (target:TechNode {name: $nodeName})
    WHERE target.level = $endLevel
    WITH target
    MATCH path = (start:TechNode)-[*]->(target)
    WHERE start.level = $startLevel
        AND all(rel IN relationships(path) WHERE rel.type = "学习路线")
    WITH relationships(path) AS rels, nodes(path) AS nodes
    UNWIND range(0, size(rels)-1) AS i
    WITH rels[i] AS rel, nodes[i] AS startNode, nodes[i+1] AS endNode
    RETURN
        {
            name: startNode.name,
            info: startNode.info,
            level: toInteger(startNode.level),
            type1: startNode.type1,
            type2: startNode.type2
        } as startNode,
        {
            name: endNode.name,
            info: endNode.info,
            level: toInteger(endNode.level),
            type1: endNode.type1,
            type2: endNode.type2
        } as endNode,
        {
            info: rel.info,
            type: rel.type
        } as nodeRelationship
    """)
    List<NodeRelVO> getNodeRelByLevel(@Param("startLevel") String startLevel,
                                      @Param("endLevel") String endLevel,
                                      @Param("nodeName") String nodeName);
}
