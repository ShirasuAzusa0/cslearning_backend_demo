package ben.qihuiadmin.entity.entity_kb;

import ben.qihuiadmin.entity.entity_kb.IdClass.ParagraphQuestion;
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
}
