package ben.qihuipost.repository;

import ben.qihuipost.entity.entity_post.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCategoryRepository extends JpaRepository<Categories, Integer> {
}
