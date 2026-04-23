package ben.qihuiadmin.repository;

import ben.qihuiadmin.entity.entity_user.UserBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBanRepository extends JpaRepository<UserBan, Long> {
    @Query("SELECT ub FROM UserBan ub WHERE ub.banEndTime <= CURRENT_TIMESTAMP")
    List<UserBan> findExpiredBans();

    @Query("SELECT ub FROM UserBan ub WHERE ub.user.userId = :userId AND ub.active = 1")
    UserBan findByUserId(@Param("userId") Long userId);
}
