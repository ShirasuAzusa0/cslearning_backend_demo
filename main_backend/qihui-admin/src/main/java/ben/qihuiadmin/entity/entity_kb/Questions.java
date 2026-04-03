package ben.qihuiadmin.entity.entity_kb;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "questions")
public class Questions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "questionId")
    private long questionId;

    @Column(name = "content")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kbId", referencedColumnName = "kbId")
    private KnowledgeBases kb;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private List<paragraph_questions> paragraphs;
}
