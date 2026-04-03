package ben.qihuiauth.entity.entity_post;

import ben.qihuiauth.entity.entity_users.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@IdClass(Favorites.class)
@Table(name = "favorites")
public class Favorites {
    @Id
    @ManyToOne
    @JoinColumn(name = "postId", referencedColumnName = "postId")
    private Posts post;

    @Id
    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private Users user;
}
