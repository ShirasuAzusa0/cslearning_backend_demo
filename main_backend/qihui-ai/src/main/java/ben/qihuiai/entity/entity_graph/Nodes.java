package ben.qihuiai.entity.entity_graph;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Node")
@Getter
@Setter
public class Nodes {
    @Id
    private String name;  // name作为唯一标识

    @Property("info")
    private String info;

    @Property("level")
    private int level;

    @Property("type1")
    private String type1;

    @Property("type2")
    private String type2;
}
