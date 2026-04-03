package ben.qihuiadmin.entity.entity_kb;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "paragraphs")
public class Paragraphs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paragraphId")
    private long paragraphId;

    @Column(name = "splitAt", nullable = false)
    private LocalDateTime splitAt;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "order", nullable = false)
    private int order;

    @Column(name = "parentChain", nullable = false)
    private String parentChain;

    @Column(name = "context", nullable = false, columnDefinition = "LONGTEXT")
    private String context;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documentId", referencedColumnName = "documentId")
    private Documents document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kbId", referencedColumnName = "kbId")
    private KnowledgeBases kb;

    @OneToMany(mappedBy = "paragraph", fetch = FetchType.LAZY)
    private List<paragraph_questions> questions;
}
