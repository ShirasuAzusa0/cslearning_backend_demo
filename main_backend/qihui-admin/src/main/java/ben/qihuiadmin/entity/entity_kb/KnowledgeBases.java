package ben.qihuiadmin.entity.entity_kb;

import ben.qihuiadmin.entity.entity_chat.Models;
import ben.qihuiadmin.entity.entity_user.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "knowledge_bases")
public class KnowledgeBases {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kbId")
    private int kbId;

    @Column(name = "kbName", nullable = false)
    private String kbName;

    @Column(name = "kbDescription", nullable = false)
    private String kbDescription;

    @Column(name = "kbDocNum", nullable = false)
    private int kbDocNum;

    @Column(name = "kbType", nullable = false)
    private String kbType;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private Users user;

    @OneToOne
    @JoinColumn(name = "modelId", referencedColumnName = "modelId")
    private Models model;

    @OneToMany(mappedBy = "kb", fetch = FetchType.LAZY)
    private List<doc_kb> documents;
}
