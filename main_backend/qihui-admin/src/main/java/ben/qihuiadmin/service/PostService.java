package ben.qihuiadmin.service;

import ben.qihuiadmin.entity.entity_post.Categories;
import ben.qihuiadmin.entity.entity_post.Posts;
import ben.qihuiadmin.entity.vo.*;
import ben.qihuiadmin.repository.PostCategoryRepository;
import ben.qihuiadmin.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static ben.qihuiadmin.entity.vo.PostListVO.getPostListVO;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;

    public PostService(PostRepository postRepository,
                       PostCategoryRepository postCategoryRepository) {
        this.postRepository = postRepository;
        this.postCategoryRepository = postCategoryRepository;
    }

    public PostDetailVO getPostDetails(long postId) {
        Posts p = postRepository.findByPostId(postId);
        if (p == null) {
            return null;
        }
        return new PostDetailVO(
                p.getPostId(),
                p.getTitle(),
                new AuthorVO(
                        p.getAuthor().getUserId(),
                        new AuthorVO.Attributes(
                                p.getAuthor().getAvatarURL(),
                                p.getAuthor().getUserName(),
                                p.getAuthor().getEmail()
                        )
                ),
                p.getTags().stream()
                        .map(tag -> new TagVO(
                                tag.getTag().getTagId(),
                                tag.getTag().getTagName()
                        )).toList(),

                p.getCreatedAt(),
                p.getLastCommentedAt(),
                p.getCommentsCount(),
                p.getLikesCount(),
                p.getFavoritesCount(),
                p.getContent(),
                p.getComments().stream()
                        .map(c -> new CommentVO(
                                c.getCommentId(),
                                new AuthorVO(
                                        c.getAuthor().getUserId(),
                                        new AuthorVO.Attributes(
                                                c.getAuthor().getAvatarURL(),
                                                c.getAuthor().getUserName(),
                                                c.getAuthor().getEmail()
                                        )
                                ),
                                c.getContent(),
                                c.getCreatedAt(),
                                c.getLikesCount(),
                                c.getRepliedId()
                        )).toList()
        );
    }

    public PostListVO getPostListByTag(int tag) {
        List<Posts> postList = postRepository.getPostsByTag(tag);
        return getPostListVO(postList);
    }

    public List<TagDetailVO> getAllTags() {
        List<Categories> tags = postCategoryRepository.findAll();
        return tags.stream()
                .map(pc -> new TagDetailVO(
                        pc.getTagId(),
                        pc.getTagName(),
                        pc.getHueColor(),
                        pc.getDescription(),
                        pc.getPostsCount(),
                        pc.getLastPostTime()
                )).toList();
    }

    @Transactional
    public long deletePostByPostId(long postId) {
        long affected = postRepository.deleteByPostId(postId);
        if(affected == 0) {
            return -1;
        }
        return postId;
    }
}
