package ben.qihuipost.entity.entity_post;

import ben.qihuipost.entity.entity_post.IdClass.PostCategories;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@IdClass(PostCategories.class)
@Table(name = "post_categories")
public class post_categories {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", referencedColumnName = "postId")
    private Posts post;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tagId", referencedColumnName = "tagId")
    private Categories tag;
}
