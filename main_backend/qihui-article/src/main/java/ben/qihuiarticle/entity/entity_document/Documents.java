package ben.qihuiarticle.entity.entity_document;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "documents")
public class Documents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "documentId")
    private long documentId;

    @Column(name = "documentName", nullable = false)
    private String documentName;

    @Column(name = "documentSize", nullable = false)
    private int documentSize;

    @Column(name = "documentParts", nullable = false)
    private int documentParts;

    @Column(name = "documentLoadedAt", nullable = false)
    private LocalDateTime documentLoadedAt;

    @Column(name = "documentType", nullable = false)
    private String documentType;

    @Column(name = "documentContent", nullable = false)
    private String documentContent;
}
