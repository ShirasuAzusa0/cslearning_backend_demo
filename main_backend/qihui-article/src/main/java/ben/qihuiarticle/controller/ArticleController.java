package ben.qihuiarticle.controller;

import ben.qihuiarticle.entity.RestBean;
import ben.qihuiarticle.entity.vo.ArticleProfileVO;
import ben.qihuiarticle.entity.vo.ArticleVO;
import ben.qihuiarticle.service.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/article")
public class ArticleController {
    private final ArticleService articleService;
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // 获取文章内容
    @GetMapping("/details")
    public ResponseEntity<?> getArticleDetails(@RequestParam(name = "articleName") String articleName,
                                               @RequestParam(name = "articlePath") String articlePath) {
        if (articleName == null || articlePath == null) {
            return ResponseEntity.badRequest().body("articleName 或 articlePath 不可为空");
        }
        ArticleVO vo = articleService.getArticleByArticleName(articleName, articlePath);
        return ResponseEntity.ok(RestBean.successType1("获取文章内容成功", vo));
    }

    // 获取指定文章的概要信息
    @GetMapping("/profile/{documentId}")
    public ResponseEntity<?> getArticleProfile(@PathVariable(name = "documentId") int documentId) {
        if (ObjectUtils.isEmpty(documentId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("documentId不能为空"));
        }
        ArticleProfileVO vo = articleService.getArticleProfileByDocumentId(documentId);
        return ResponseEntity.ok(RestBean.successType1("获取文章概要信息成功", vo));
    }

    // 获取文章概要信息列表
    @GetMapping("/profile/list")
    public ResponseEntity<?> getAllArticleProfiles() {
        List<ArticleProfileVO> vos = articleService.getArticleProfileList();
        return ResponseEntity.ok(RestBean.successType1("获取文章概要信息列表成功", vos));
    }

    // 获取定制化推荐学习的文章列表
    @GetMapping("/recommend/list")
    public ResponseEntity<?> getRecommendArticleProfiles(@RequestParam(name = "userId") long userId) {
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }
        List<ArticleProfileVO> vos = articleService.getRecommendListByUserId(userId);
        return ResponseEntity.ok(RestBean.successType1("获取推荐学习的文章列表成功", vos));
    }
}
