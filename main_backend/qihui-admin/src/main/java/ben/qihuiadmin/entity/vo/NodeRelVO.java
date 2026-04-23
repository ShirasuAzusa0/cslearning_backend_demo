package ben.qihuiadmin.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeRelVO {
    private NodeVO startNode;
    private NodeVO endNode;
    private RelationshipVO nodeRelationship;

    // 单参数构造函数（用于孤立节点）
    public NodeRelVO(NodeVO startNode) {
        this.startNode = startNode;
        this.endNode = null;
        this.nodeRelationship = null;
    }
}
