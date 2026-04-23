package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_post.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCategoryRepository extends JpaRepository<Categories, Integer> {
}
