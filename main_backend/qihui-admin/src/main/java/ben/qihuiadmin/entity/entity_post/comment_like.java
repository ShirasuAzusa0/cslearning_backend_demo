package ben.qihuiadmin.entity.entity_post;

import ben.qihuiadmin.entity.entity_post.IdClass.CommentLikes;
import ben.qihuiadmin.entity.entity_user.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@IdClass(CommentLikes.class)
@Table(name = "comment_like")
public class comment_like {
    @Id
    @ManyToOne
    @JoinColumn(name = "commentId", referencedColumnName = "commentId")
    private Comments comment;

    @Id
    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private Users user;
}
