package ben.qihuiauth.service;

import ben.qihuiauth.entity.entity_post.Posts;
import ben.qihuiauth.entity.vo.PostListVO;
import ben.qihuiauth.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import static ben.qihuiauth.entity.vo.PostListVO.getPostListVO;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // 获取热门帖子列表（获取三个）
    public PostListVO getPopularPostList() {
        List<Posts> popularPostsList = postRepository.findPopularPosts(3);
        return getPostListVO(popularPostsList);
    }
}
