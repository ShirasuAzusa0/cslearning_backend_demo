package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_post.Comments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comments, Long> {
}
