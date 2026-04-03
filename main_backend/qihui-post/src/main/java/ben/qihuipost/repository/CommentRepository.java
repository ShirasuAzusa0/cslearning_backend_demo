package ben.qihuipost.repository;

import ben.qihuipost.entity.entity_post.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comments, Integer> {
    // Hibernate 不会自动把 1/0 转成 boolean，不支持 boolean
    @Query(value = """
            SELECT CASE
                 WHEN EXISTS (
                     SELECT 1
                     FROM comment_like
                     WHERE commentId = :commentId
                      AND userId = :userId
                )
                THEN TRUE
                ELSE FALSE
            END
            """, nativeQuery = true)
    long likedCheck(@Param("commentId") long commentId, @Param("userId") long userId);

    Comments findByCommentId(@Param("commentId") long commentId);

    @Modifying
    @Query(value = """
            INSERT INTO comment_like (commentId, userId)
            VALUES (:commentId, :userId)
            """, nativeQuery = true)
    void insert(@Param("commentId") long commentId, @Param("userId") long userId);

    @Modifying
    @Query(value = """
            DELETE FROM comment_like
            WHERE commentId = :commentId
              AND userId = :userId
            """, nativeQuery = true)
    void deleteByCommentIdAndUserId(@Param("commentId") long commentId, @Param("userId") long userId);
}
