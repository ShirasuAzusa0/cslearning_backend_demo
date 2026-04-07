package ben.qihuipost.repository;

import ben.qihuipost.entity.entity_post.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriesRepository extends JpaRepository<Categories, Long> {

    Categories findByTagId(int tagId);
}
