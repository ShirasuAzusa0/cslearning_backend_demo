package ben.qihuiai.entity.entity_chat;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "messages")
public class Messages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "messageId")
    private long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionId", referencedColumnName = "sessionId")
    private Sessions session;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "tokens", nullable = false)
    private long tokens;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "role", nullable = false, columnDefinition = "enum('system','user','assistant')")
    @Enumerated(EnumType.STRING)
    private roleType role;

    @Column(name = "referenceData", columnDefinition = "json")
    private String referenceData;
}
