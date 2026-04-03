package ben.qihuipost.entity.entity_post;

import ben.qihuipost.entity.entity_users.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "comments")
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commentId")
    private long commentId;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @OneToOne
    @JoinColumn(name = "authorId", referencedColumnName = "userId")
    private Users author;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "likesCount", nullable = false)
    private int likesCount;

    @Column(name = "repliedId")
    private Long repliedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", referencedColumnName = "postId")
    private Posts post;
}
