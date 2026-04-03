package ben.qihuipost.controller;

import ben.qihuipost.entity.RestBean;
import ben.qihuipost.entity.vo.PostListVO;
import ben.qihuipost.entity.vo.ProfileVO;
import ben.qihuipost.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 获取用户信息
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable(name = "userId") long userId) {
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }
        ProfileVO vo = userService.getUserById(userId);

        return ResponseEntity.ok(RestBean.successType1("获取用户信息成功", vo));
    }

    // 获取用户发表的帖子列表
    @GetMapping("/profile/{userId}/posts")
    public ResponseEntity<?> getPosts(@PathVariable(name = "userId") long userId) {
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }

        PostListVO vo = userService.getUserPosts(userId, "released");

        return ResponseEntity.ok(RestBean.successType1("获取用户发表的帖子列表成功", vo));
    }

    // 获取用户收藏的帖子的列表
    @GetMapping("/forum/{userId}/favorite")
    public ResponseEntity<?> getFavoritePosts(@PathVariable(name = "userId") long userId) {
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }

        PostListVO vo = userService.getUserPosts(userId, "favorite");

        return ResponseEntity.ok(RestBean.successType1("获取用户收藏的帖子列表成功", vo));
    }
}
