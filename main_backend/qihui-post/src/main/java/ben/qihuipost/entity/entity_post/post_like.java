package ben.qihuipost.entity.entity_post;

import ben.qihuipost.entity.entity_post.IdClass.PostLikes;
import ben.qihuipost.entity.entity_users.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@IdClass(PostLikes.class)
@Table(name = "post_like")
public class post_like {
    @Id
    @ManyToOne
    @JoinColumn(name = "postId", referencedColumnName = "postId")
    private Posts post;

    @Id
    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private Users user;
}
