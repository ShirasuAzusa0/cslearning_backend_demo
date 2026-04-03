package ben.qihuiauth.repository;

import ben.qihuiauth.entity.entity_users.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Users findByUserId(long userId);

    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Users findByEmail(@Param("email") String email);
}
