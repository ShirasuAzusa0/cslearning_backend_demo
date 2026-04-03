package ben.qihuiarticle.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeRelVO {
    private NodeVO startNode;
    private NodeVO endNode;
    private RelationshipVO relationship;
}
