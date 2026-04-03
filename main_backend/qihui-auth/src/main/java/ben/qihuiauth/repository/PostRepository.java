package ben.qihuiauth.repository;

import ben.qihuiauth.entity.entity_post.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Posts, Long> {
    // 采用 Hacker News 排名算法的变体来计算获取热门帖子
    @Query(value = """
        SELECT p.*
        FROM posts p
        INNER JOIN (
            SELECT postId,
                   (favoritesCount * 1 + likesCount * 3 + commentsCount * 5) /
                   POW((UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(createdAt)) / 3600 + 2, 1.5) AS hot_score
            FROM posts
        ) AS scored ON p.postId = scored.postId
        ORDER BY scored.hot_score DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Posts> findPopularPosts(@Param("limit") int limit);
}
