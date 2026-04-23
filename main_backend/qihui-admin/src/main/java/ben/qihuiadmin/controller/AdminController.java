package ben.qihuiadmin.controller;

import ben.qihuiadmin.entity.RestBean;
import ben.qihuiadmin.entity.vo.*;
import ben.qihuiadmin.service.KGService;
import ben.qihuiadmin.service.PostService;
import ben.qihuiadmin.service.UserService;
import ben.qihuiadmin.service.adminService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final PostService postService;
    private final KGService kgService;
    private final adminService adminService;

    public AdminController(UserService userService,
                           PostService postService,
                           KGService kgService, adminService adminService) {
        this.userService = userService;
        this.postService = postService;
        this.kgService = kgService;
        this.adminService = adminService;
    }

    // 获取管理员列表
    @GetMapping("/list")
    public ResponseEntity<?> getAdminList() {
        List<UserElementVO> vos = userService.findAdminList();
        return ResponseEntity.ok(RestBean.successType1("获取管理员列表成功", vos));
    }

    // 获取用户列表
    @GetMapping("/user/list")
    public ResponseEntity<?> getUserList() {
        List<UserProfileVO> vos = userService.findUserList();
        return ResponseEntity.ok(RestBean.successType1("获取用户列表成功", vos));
    }

    // 获取用户详细信息
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable(name = "userId") Long userId) {
        UserDetailVO vo = userService.findUserDetails(userId);
        return ResponseEntity.ok(RestBean.successType1("获取用户详细信息成功", vo));
    }

    // 用户封禁
    @PostMapping("/user/{userId}/ban")
    public ResponseEntity<?> banUser(@PathVariable(name = "userId") Long userId,
                                     @RequestParam(name = "adminId") Long adminId,
                                     @RequestParam(name = "reason") String reason,
                                     @RequestParam(name = "expire") Integer expire) {
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }
        long resId = userService.banUserById(userId, adminId, reason, expire);
        if (resId < 0) {
            return ResponseEntity.badRequest().body("用户封禁失败");
        }
        return ResponseEntity.ok(RestBean.successType1("用户封禁成功", resId));
    }

    // 用户解封
    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable(name = "userId")Long userId) {
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }
        long resId = userService.unbanUserById(userId);
        if (resId < 0) {
            return ResponseEntity.badRequest().body("用户解封失败");
        }
        return ResponseEntity.ok(RestBean.successType1("用户解封成功", resId));
    }

    // 删除用户
    @DeleteMapping("/user/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable(name = "userId") Long userId) {
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }
        long resId = userService.deleteUserByUserId(userId);
        if (resId < 0) {
            return ResponseEntity.badRequest().body(RestBean.failure("用户删除失败"));
        }
        return ResponseEntity.ok(RestBean.successType1("删除用户成功", resId));
    }

    // 获取指定标签对应的帖子列表
    @GetMapping("/forum/{tag}/posts")
    public ResponseEntity<?> getForumPosts(@PathVariable(name = "tag") int tag) {
        if (ObjectUtils.isEmpty(tag)) {
            return ResponseEntity.badRequest().body(RestBean.failure("tag不能为空"));
        }
        PostListVO vos = postService.getPostListByTag(tag);
        return ResponseEntity.ok(RestBean.successType1("通过指定标签获取帖子列表成功", vos));
    }

    // 获取具体帖子内容
    @GetMapping("/forum/{postId}")
    public ResponseEntity<?> getForumDetails(@PathVariable(name = "postId") Long postId) {
        if (ObjectUtils.isEmpty(postId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("postId不能为空"));
        }
        PostDetailVO vo = postService.getPostDetails(postId);
        return ResponseEntity.ok(RestBean.successType1("获取具体帖子内容成功", vo));
    }

    // 获取所有标签列表
    @GetMapping("/forum/tags/list")
    public ResponseEntity<?> getAllTags() {
        List<TagDetailVO> vos = postService.getAllTags();
        return ResponseEntity.ok(RestBean.successType1("获取标签列表成功", vos));
    }

    // 删除帖子
    @DeleteMapping("/forum/post/delete/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable(name = "postId") Long postId) {
        if (ObjectUtils.isEmpty(postId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("postId不能为空"));
        }
        long resId = postService.deletePostByPostId(postId);
        if (resId < 0) {
            return ResponseEntity.badRequest().body(RestBean.failure("帖子删除失败"));
        }
        return ResponseEntity.ok(RestBean.successType1("删除帖子成功", resId));
    }

    // 获取所有图谱节点与关系
    @GetMapping("/knowledge_graph")
    public ResponseEntity<?> getAllNodeRel() {
        List<NodeRelVO> vos = kgService.findAllNodeRel();
        return ResponseEntity.ok(RestBean.successType1("获取所有图谱节点关系成功", vos));
    }

    // 图谱更新
    @PostMapping("/knowledge_graph/update")
    public ResponseEntity<?> updateNodeRel(@RequestPart(required = false, name = "graphFile")MultipartFile graphFile) throws Exception {
        if (ObjectUtils.isEmpty(graphFile)) {
            return ResponseEntity.badRequest().body("上传文件出错");
        }
        if (!Objects.requireNonNull(graphFile.getOriginalFilename()).endsWith(".json")) {
            return ResponseEntity.badRequest().body("只支持 json 文件");
        }
        kgService.updateNodeAndRel(graphFile);
        return ResponseEntity.ok(RestBean.successType2("图谱数据更新成功"));
    }

    // 获取平台数据统计
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        DataStatsVO vo = adminService.getStats();
        return ResponseEntity.ok(RestBean.successType1("获取平台统计数据成功", vo));
    }
}
