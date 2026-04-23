package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_post.comment_like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<comment_like, Long> {
}
