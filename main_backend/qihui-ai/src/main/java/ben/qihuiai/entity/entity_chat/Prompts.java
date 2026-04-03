package ben.qihuiai.entity.entity_chat;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "prompts")
public class Prompts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promptId")
    private int promptId;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;
}
