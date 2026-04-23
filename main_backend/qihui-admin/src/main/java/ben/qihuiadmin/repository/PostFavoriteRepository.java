package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_post.Favorites;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostFavoriteRepository extends JpaRepository<Favorites, Long> {
}
