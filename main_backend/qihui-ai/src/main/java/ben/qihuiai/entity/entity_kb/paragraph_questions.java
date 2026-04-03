package ben.qihuiai.entity.entity_kb;

import ben.qihuiai.entity.entity_kb.IdClass.ParagraphQuestion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@IdClass(ParagraphQuestion.class)
public class paragraph_questions {
    @Id
    @ManyToOne
    @JoinColumn(name = "paragraphId", referencedColumnName = "paragraphId")
    private Paragraphs paragraph;

    @Id
    @ManyToOne
    @JoinColumn(name = "questionId", referencedColumnName = "questionId")
    private Questions question;

    @ManyToOne
    @JoinColumn(name = "kbId", referencedColumnName = "kbId")
    private KnowledgeBases kb;

    @ManyToOne
    @JoinColumn(name = "documentId", referencedColumnName = "documentId")
    private Documents document;
}
