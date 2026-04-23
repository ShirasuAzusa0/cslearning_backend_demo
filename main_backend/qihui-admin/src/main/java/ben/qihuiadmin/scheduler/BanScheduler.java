package ben.qihuiadmin.scheduler;

import ben.qihuiadmin.entity.entity_user.UserBan;
import ben.qihuiadmin.repository.UserBanRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class BanScheduler {
    private final UserBanRepository userBanRepository;

    public BanScheduler(UserBanRepository userBanRepository) {
        this.userBanRepository = userBanRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void unbanExpiredUsers() {
        List<UserBan> expiredBans = userBanRepository.findExpiredBans();

        if (expiredBans.isEmpty()) {
            return;
        }

        for (UserBan ub : expiredBans) {
            // 恢复用户状态（解禁解封）
            ub.setActive(0);
            userBanRepository.save(ub);
        }
    }
}
