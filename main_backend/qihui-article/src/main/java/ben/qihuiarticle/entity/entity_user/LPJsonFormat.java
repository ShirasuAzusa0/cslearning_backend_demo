package ben.qihuiarticle.entity.entity_user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LPJsonFormat {
    private NodeInfo startNode;
    private NodeInfo endNode;
    private RelationshipInfo nodeRelationship;

    @Data
    public static class NodeInfo {
        private String name;
        private String info;
        private Integer level;
        private String type1;
        private String type2;
    }

    @Data
    public static class RelationshipInfo {
        private String info;
        private String type;
    }
}