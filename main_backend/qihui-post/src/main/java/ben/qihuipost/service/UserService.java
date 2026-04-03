package ben.qihuipost.service;

import ben.qihuipost.entity.entity_post.Posts;
import ben.qihuipost.entity.entity_users.Users;
import ben.qihuipost.entity.vo.PostListVO;
import ben.qihuipost.entity.vo.ProfileVO;
import ben.qihuipost.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import static ben.qihuipost.entity.vo.PostListVO.getPostListVO;
import static ben.qihuipost.util.jsonUtil.LPtoJSON;


@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileVO getUserById(long userId) {
        Users user = userRepository.findByUserId(userId);

        return new ProfileVO(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getAvatarURL(),
                user.getSelfDescription(),
                user.getLastConnectedDate(),
                new ProfileVO.Counts(
                        user.getReplies(),
                        user.getTopics(),
                        user.getFollower(),
                        user.getFollower()
                ),
                LPtoJSON(user.getLearningPath()),
                user.getLearningPathDescription()
        );
    }

    public PostListVO getUserPosts(long userId, String type) {
        List<Posts> postList = null;

        if (type.equals("released")) {
            postList = userRepository.findReleasedPosts(userId);
        }

        else if (type.equals("favorite")) {
            postList = userRepository.findFavoritePosts(userId);
        }

        if (postList != null && postList.isEmpty()) {
            return null;
        }
        return getPostListVO(postList);
    }
}
