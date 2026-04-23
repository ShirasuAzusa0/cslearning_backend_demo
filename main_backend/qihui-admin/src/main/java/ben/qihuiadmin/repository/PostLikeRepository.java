package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_post.post_like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<post_like, Long> {
}
