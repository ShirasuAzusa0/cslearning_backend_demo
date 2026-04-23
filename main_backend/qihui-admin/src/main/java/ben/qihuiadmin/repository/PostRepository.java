package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_post.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Posts, Long> {
    Posts findByPostId(@Param("postId") long postId);

    @Query(value = """
            SELECT p
            FROM Posts p
            JOIN post_categories pc ON p.postId = pc.post.postId
            JOIN Categories t ON t.tagId = pc.tag.tagId
            WHERE t.tagId = :tagId
            """)
    List<Posts> getPostsByTag(@Param("tagId") int tagId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Posts p Where p.postId = :postId")
    int deleteByPostId(@Param("postId") Long postId);
}
