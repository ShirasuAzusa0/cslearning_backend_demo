package ben.qihuiai.entity.entity_kb;

import ben.qihuiai.entity.entity_kb.IdClass.DocKb;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@IdClass(DocKb.class)
@Table(name = "doc_kb")
public class doc_kb {
    @Id
    @ManyToOne
    @JoinColumn(name = "documentId", referencedColumnName = "documentId")
    private Documents document;

    @Id
    @ManyToOne
    @JoinColumn(name = "kbId", referencedColumnName = "kbId")
    private KnowledgeBases kb;
}
