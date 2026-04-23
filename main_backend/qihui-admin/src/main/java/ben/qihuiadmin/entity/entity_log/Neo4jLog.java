package ben.qihuiadmin.entity.entity_log;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "neo4j_log")
public class Neo4jLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "neo4jLogId")
    private long neo4jLogId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "logDate", nullable = false)
    private LocalDateTime logDate;
}
