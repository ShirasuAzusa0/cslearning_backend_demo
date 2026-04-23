package ben.qihuiadmin.service;

import ben.qihuiadmin.entity.entity_user.UserBan;
import ben.qihuiadmin.entity.entity_user.Users;
import ben.qihuiadmin.entity.entity_user.userType;
import ben.qihuiadmin.entity.vo.UserDetailVO;
import ben.qihuiadmin.entity.vo.UserElementVO;
import ben.qihuiadmin.entity.vo.UserProfileVO;
import ben.qihuiadmin.repository.UserBanRepository;
import ben.qihuiadmin.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static ben.qihuiadmin.util.jsonUtil.NodeRelToJSON;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserBanRepository userBanRepository;

    public UserService(UserRepository userRepository, UserBanRepository userBanRepository) {
        this.userRepository = userRepository;
        this.userBanRepository = userBanRepository;
    }

    public List<UserElementVO> findAdminList() {
        List<Users> userList = userRepository.findByUserType(userType.admin);
        return userList.stream()
                .map(u -> new UserElementVO(
                        u.getUserName(),
                        u.getEmail(),
                        u.getLastConnectedDate()
                ))
                .toList();
    }

    public List<UserProfileVO> findUserList() {
        List<Users> userList = userRepository.findByUserType(userType.user);
        return userList.stream()
                .map(u -> new UserProfileVO(
                        u.getUserId(),
                        u.getUserName(),
                        u.getEmail(),
                        u.getType().toString(),
                        u.getReplies(),
                        u.getTopics(),
                        u.getFollower(),
                        u.getFollowing(),
                        u.getLastConnectedDate()
                ))
                .toList();
    }

    public UserDetailVO findUserDetails(long userId) {
        Users u = userRepository.findByUserId(userId);
        return new UserDetailVO(
                u.getUserId(),
                u.getUserName(),
                u.getEmail(),
                u.getType().toString(),
                u.getReplies(),
                u.getTopics(),
                u.getFollower(),
                u.getFollowing(),
                u.getSelfDescription(),
                u.getLastConnectedDate(),
                u.getAvatarURL(),
                NodeRelToJSON(u.getLearningPath()),
                u.getLearningPathDescription()
        );
    }

    @Transactional
    public long deleteUserByUserId(long userId) {
        long affected = userRepository.deleteByUserId(userId);
        if(affected == 0) {
            return -1;
        }
        return userId;
    }

    public long banUserById(long userId, long adminId, String reason, int expire) {
        UserBan ub = userBanRepository.findByUserId(userId);
        if(ub != null) {
            ub.setActive(0);
            userBanRepository.save(ub);
        }
        UserBan newUB = new UserBan();
        newUB.setUser(userRepository.findByUserId(userId));
        newUB.setAdmin(userRepository.findByUserId(adminId));
        newUB.setBanReason(reason);
        newUB.setBanStartTime(LocalDateTime.now());
        newUB.setBanEndTime(LocalDateTime.now().plusDays(expire));
        newUB.setActive(1);
        userBanRepository.save(newUB);
        return userId;
    }

    public long unbanUserById(long userId) {
        UserBan ub = userBanRepository.findByUserId(userId);
        if(ub == null) {
            return -1;
        }
        ub.setActive(0);
        userBanRepository.save(ub);
        return userId;
    }
}
