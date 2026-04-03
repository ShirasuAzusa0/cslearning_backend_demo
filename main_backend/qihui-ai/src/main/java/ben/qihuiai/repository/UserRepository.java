package ben.qihuiai.repository;

import ben.qihuiai.entity.entity_user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Users findByUserId(@Param("userId") long userId);

    @Query(value = "SELECT u FROM Users u where u.email = :email")
    Users findByEmail(@Param("email") String email);
}
