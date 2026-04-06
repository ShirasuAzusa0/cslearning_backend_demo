package ben.qihuipost.controller;

import ben.qihuipost.entity.RestBean;
import ben.qihuipost.entity.dto.NewCommentDto;
import ben.qihuipost.entity.dto.NewPostDto;
import ben.qihuipost.entity.vo.*;
import ben.qihuipost.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 获取标签列表
    @GetMapping("/tags/list")
    public ResponseEntity<?> getForumTags() {
        List<TagDetailVO> vos = postService.getAllTags();
        return ResponseEntity.ok(RestBean.successType1("获取标签列表成功", vos));
    }

    // 获取帖子列表
    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(@RequestParam(name = "limit") int limit,
                                      @RequestParam(name = "start") int start,
                                      @RequestParam(name = "method") int method) {
        PostListVO vos = postService.getPostList(limit, start, method);
        if (vos.getPosts() == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("使用了错误的method"));
        }
        return ResponseEntity.ok(RestBean.successType1("帖子列表获取成功", vos));
    }

    // 获取指定标签对应的帖子列表
    @GetMapping("/{tag}/posts")
    public ResponseEntity<?> getPostsByTag(@PathVariable(name = "tag") int tag) {
        if (ObjectUtils.isEmpty(tag)) {
            return ResponseEntity.badRequest().body(RestBean.failure("tag不能为空"));
        }
        PostListVO vos = postService.getPostListByTag(tag);
        return ResponseEntity.ok(RestBean.successType1("通过指定标签获取帖子列表成功", vos));
    }

    // 获取具体帖子内容
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetailById(@PathVariable(name = "postId") long postId,
                                               @RequestParam(name = "userId") long userId) {
        if (ObjectUtils.isEmpty(postId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("postId不能为空"));
        }
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }
        PostDetailVO vo = postService.getPostDetails(postId, userId);
        return ResponseEntity.ok(RestBean.successType1("获取具体帖子内容成功", vo));
    }

    // 发布新帖子
    @PostMapping("/newpost")
    public ResponseEntity<?> newPost(@RequestBody NewPostDto dto, @RequestParam(name = "userId") long userId) {
        if (dto.getTitle() == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("title不能为空"));
        }
        if (dto.getContent() == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("content不能为空"));
        }
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }
        NewPostVO vo = postService.newPost(dto, userId);
        return ResponseEntity.ok(RestBean.successType1("新帖子发布成功", vo));
    }

    // 发布新评论（评论帖子 or 回复评论）
    @PostMapping("/newcomment")
    public ResponseEntity<?> newComment(@RequestBody NewCommentDto dto,
                                        @RequestParam(name = "userId") long userId,
                                        @RequestParam(name = "postId") long postId) {
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }
        if (ObjectUtils.isEmpty(postId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("postId不能为空"));
        }
        NewCommentVO vo = postService.newComment(dto, userId, postId);
        return ResponseEntity.ok(RestBean.successType1("评论发布成功", vo));
    }

    // 点赞帖子/评论
    @PutMapping("/{type}/like")
    public ResponseEntity<?> like(@PathVariable(name = "type") String type,
                                  @RequestParam(name = "userId") long userId,
                                  @RequestParam(name = "likeId") long likeId) {
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }
        if (ObjectUtils.isEmpty(likeId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("likeId不能为空"));
        }
        LikeVO vo = postService.Like(type, userId, likeId);
        if (vo == null) return ResponseEntity.badRequest().body(RestBean.failure("type不合规"));
        else if (vo.isLiked())
            return ResponseEntity.ok(RestBean.successType1("点赞成功", vo));
        else
            return ResponseEntity.ok(RestBean.successType1("取消点赞成功", vo));
    }

    // 收藏帖子
    @PutMapping("/{postId}/favorite")
    public ResponseEntity<?> favorite(@PathVariable(name = "postId") long postId,
                                      @RequestParam(name = "userId") long userId) {
        if (ObjectUtils.isEmpty(postId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("postId不能为空"));
        }
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }
        FavoriteVO vo = postService.Favorite(postId, userId);
        if (vo.isFavorite())
            return ResponseEntity.ok(RestBean.successType1("收藏成功", vo));
        else
            return ResponseEntity.ok(RestBean.successType1("取消收藏成功", vo));
    }
}
