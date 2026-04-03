package ben.qihuiauth.controller;

import ben.qihuiauth.entity.RestBean;
import ben.qihuiauth.entity.vo.PostListVO;
import ben.qihuiauth.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class PopularController {
    private final PostService postService;

    public PopularController(PostService postService) {
        this.postService = postService;
    }

    // 获取热门帖子列表
    @GetMapping("/popular/posts")
    public ResponseEntity<?> getPopularPosts() {
        PostListVO vos = postService.getPopularPostList();
        return ResponseEntity.ok(RestBean.successType1("返回热门帖子列表成功", vos));
    }
}
