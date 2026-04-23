package ben.qihuiadmin.entity.entity_user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "user_ban")
public class UserBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banId")
    private int banId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adminId", referencedColumnName = "userId")
    private Users admin;

    @Column(name = "banReason", nullable = false)
    private String banReason;

    @Column(name = "banStartTime", nullable = false)
    private LocalDateTime banStartTime;

    @Column(name = "banEndTime", nullable = false)
    private LocalDateTime banEndTime;

    @Column(name = "active", nullable = false)
    private int active;
}
