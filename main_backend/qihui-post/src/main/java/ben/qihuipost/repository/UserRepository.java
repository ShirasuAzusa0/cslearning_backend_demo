package ben.qihuipost.repository;

import ben.qihuipost.entity.entity_post.Posts;
import ben.qihuipost.entity.entity_users.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Users findByUserId(@Param("userId") long userId);
    
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Users findByEmail(@Param("email") String email);

    @Query(value = """
            SELECT p
            FROM Posts p
            WHERE p.author.userId = :userId
    """)
    List<Posts> findReleasedPosts(@Param("userId") long userId);

    @Query(value = """
            SELECT p
            FROM Posts p
            JOIN Favorites f ON p.postId = f.post.postId
            JOIN Users u ON u.userId = f.user.userId
            WHERE u.userId = :userId
    """)
    List<Posts> findFavoritePosts(@Param("userId") long userId);
}
