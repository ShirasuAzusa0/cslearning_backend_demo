package ben.qihuiarticle.entity.entity_user;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private long userId;

    @Column(name = "avatarURL", nullable = false)
    private String avatarURL;

    @Column(name = "userName", nullable = false)
    private String userName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "type", nullable = false, columnDefinition = "enum('user', 'admin')")
    @Enumerated(EnumType.STRING)
    private userType type;

    @Column(name = "selfDescription", nullable = false)
    private String selfDescription;

    @Column(name = "lastConnectedDate", nullable = false)
    private LocalDateTime lastConnectedDate;

    @Column(name = "replies", nullable = false)
    private int replies;

    @Column(name = "topics", nullable = false)
    private int topics;

    @Column(name = "follower", nullable = false)
    private int follower;

    @Column(name = "following", nullable = false)
    private int following;

    @Column(name = "learningPath", columnDefinition = "json")
    private String learningPath;

    @Column(name = "bktTable", columnDefinition = "json")
    private String bktTable;

    @Column(name = "learningPathDescription")
    private String learningPathDescription;
}
