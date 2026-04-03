package ben.qihuiadmin.entity.entity_kb;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "chunking_rules")
public class ChunkingRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ruleId")
    private int ruleId;

    @Column(name = "ruleName", nullable = false)
    private String ruleName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "regex", nullable = false)
    private String regex;

    @Column(name = "ruleType", nullable = false)
    private String ruleType;

    @Column(name = "order", nullable = false)
    private int order;
}
