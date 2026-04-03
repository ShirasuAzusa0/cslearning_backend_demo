package ben.qihuiauth.entity.entity_post;

import ben.qihuiauth.entity.entity_users.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "posts")
public class Posts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postId")
    private long postId;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorId", referencedColumnName = "userId")
    private Users author;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "lastCommentedAt", nullable = false)
    private LocalDateTime lastCommentedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lastCommentedUserId", referencedColumnName = "userId")
    private Users lastCommentedUser;

    @Column(name = "commentsCount", nullable = false)
    private int commentsCount;

    @Column(name = "likesCount", nullable = false)
    private int likesCount;

    @Column(name = "favoritesCount", nullable = false)
    private int favoritesCount;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<post_categories> tags;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<Comments> comments;
}
