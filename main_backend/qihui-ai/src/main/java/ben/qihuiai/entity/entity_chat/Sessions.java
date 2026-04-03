package ben.qihuiai.entity.entity_chat;

import ben.qihuiai.entity.entity_user.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "sessions")
public class Sessions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sessionId")
    private int sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = false)
    private Users user;

    @Column(name = "sessionName", nullable = false)
    private String sessionName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelId", referencedColumnName = "modelId", nullable = false)
    private Models model;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "lastUpdatedAt", nullable = false)
    private LocalDateTime lastUpdatedAt;
}
