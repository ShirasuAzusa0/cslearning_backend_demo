package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_user.Users;
import ben.qihuiadmin.entity.entity_user.userType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<Users, Long> {
    @Query("SELECT u FROM Users u WHERE u.type = :type")
    List<Users> findByUserType(@Param("type") userType type);

    @Query("SELECT u FROM Users u WHERE u.userId = :userId")
    Users findByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Users u Where u.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    int countUsersByType(userType type);
}
