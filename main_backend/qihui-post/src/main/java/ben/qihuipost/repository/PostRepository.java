package ben.qihuipost.repository;

import ben.qihuipost.entity.entity_post.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Posts, Integer> {
    @Query(value = """
            SELECT p
            FROM Posts p
            ORDER BY p.lastCommentedAt
            ASC LIMIT :limit
            OFFSET :offset
            """)
    List<Posts> getPostsByLastCommentedAt(@Param("limit") int limit,
                                          @Param("offset") int offset);

    @Query(value = """
            SELECT p
            FROM Posts p
            ORDER BY p.likesCount
            ASC LIMIT :limit
            OFFSET :offset
            """)
    List<Posts> getPostsByLikesCount(@Param("limit") int limit,
                                     @Param("offset") int offset);

    @Query(value = """
            SELECT p
            FROM Posts p
            ORDER BY p.createdAt
            ASC LIMIT :limit
            OFFSET :offset
            """)
    List<Posts> getPostsByCreatedAt(@Param("limit") int limit,
                                    @Param("offset") int offset);

    @Query(value = """
            SELECT p.*
            FROM posts p
            JOIN post_categories pc ON p.postId = pc.postId
            JOIN categories t ON t.tagId = pc.tagId
            WHERE t.tagId = :tagId
            """, nativeQuery = true)
    List<Posts> getPostsByTag(@Param("tagId") int tagId);

    Posts getPostByPostId(@Param("postId")long postId);

    // Hibernate 不会自动把 1/0 转成 boolean，不支持 boolean
    @Query(value = """
            SELECT CASE
                 WHEN EXISTS (
                     SELECT 1
                     FROM post_like pl
                     WHERE pl.post.postId = :postId
                      AND pl.user.userId = :userId
                )
                THEN 1
                ELSE 0
            END
            """)
    int likedCheck(@Param("postId") long postId,
                    @Param("userId") long userId);

    @Query(value = """
            SELECT CASE
                 WHEN EXISTS (
                     SELECT 1
                     FROM Favorites f
                     WHERE f.post.postId = :postId
                      AND f.user.userId = :userId
                )
                THEN 1
                ELSE 0
            END
            """)
    int favoriteCheck(@Param("postId") long postId,
                       @Param("userId") long userId);

    @Query(value = """
            SELECT p
            FROM Posts p
            WHERE p.title = :title AND p.author.userId = :userId
            ORDER BY p.createdAt DESC
            LIMIT 1
            """)
    Posts findByPostTitle(@Param("title") String title, @Param("userId") long userId);

    Posts findByPostId(@Param("postId") long postId);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            INSERT INTO post_like (postId, userId)
            VALUES (:postId, :userId)
            """, nativeQuery = true)
    void insert_like(@Param("postId") long postId,
                     @Param("userId") long userId);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            INSERT INTO favorites (postId, userId)
            VALUES (:postId, :userId)
            """, nativeQuery = true)
    void insert_favorite(@Param("postId") long postId,
                         @Param("userId") long userId);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            DELETE FROM post_like pl
            WHERE pl.post.postId = :postId
              AND pl.user.userId = :userId
            """)
    void deleteByPostIdAndUserIdForLike(@Param("postId") long postId,
                                        @Param("userId") long userId);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            DELETE FROM Favorites f
            WHERE f.post.postId = :postId
              AND f.user.userId = :userId
            """)
    void deleteByPostIdAndUserIdForFavorite(@Param("postId") long postId,
                                            @Param("userId") long userId);
}
