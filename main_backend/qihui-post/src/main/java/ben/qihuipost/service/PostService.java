package ben.qihuipost.service;

import ben.qihuipost.entity.dto.NewCommentDto;
import ben.qihuipost.entity.dto.NewPostDto;
import ben.qihuipost.entity.entity_post.Categories;
import ben.qihuipost.entity.entity_post.Comments;
import ben.qihuipost.entity.entity_post.Posts;
import ben.qihuipost.entity.entity_post.post_categories;
import ben.qihuipost.entity.vo.*;
import ben.qihuipost.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

import static ben.qihuipost.entity.vo.PostListVO.getPostListVO;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final post_categoriesRepository PCRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository,
                       PostCategoryRepository postCategoryRepository,
                       post_categoriesRepository PCRepository,
                       CommentRepository commentRepository,
                       UserRepository userRepository) {
        this.postRepository = postRepository;
        this.postCategoryRepository = postCategoryRepository;
        this.PCRepository = PCRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
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

    public PostListVO getPostList(@RequestParam(name = "limit") int limit,
                                  @RequestParam(name = "start") int start,
                                  @RequestParam(name = "method") int method) {
        List<Posts> postList;

        switch (method) {
            case 0:
                postList = postRepository.getPostsByLastCommentedAt(limit, start);
                break;
            case 1:
                postList = postRepository.getPostsByLikesCount(limit, start);
                break;
            case 2:
                postList = postRepository.getPostsByCreatedAt(limit, start);
                break;
            default:
                return null;
        }

        return getPostListVO(postList);
    }

    public PostListVO getPostListByTag(int tag) {
        List<Posts> postList = postRepository.getPostsByTag(tag);
        return getPostListVO(postList);
    }

    public PostDetailVO getPostDetails(long postId, long userId) {
        Posts p = postRepository.getPostByPostId(postId);
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
                postRepository.likedCheck(postId, userId) > 0,
                postRepository.favoriteCheck(postId, userId) > 0,
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
                                commentRepository.likedCheck(c.getCommentId(), userId) > 0,
                                c.getRepliedId()
                        )).toList()
        );
    }

    public NewPostVO newPost(NewPostDto dto, long userId) {
        Posts post = new Posts();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCreatedAt(LocalDateTime.now());
        post.setLastCommentedAt(null);
        post.setLastCommentedUser(null);
        post.setAuthor(userRepository.findByUserId(userId));
        post.setLikesCount(0);
        postRepository.save(post);

        // 处理存储新帖子的 tag 分类
        for (TagVO tag : dto.getTags()) {
            post_categories pc = new post_categories();
            pc.setPost(post);
            pc.setTag(new Categories() {{
                setTagId(tag.getTagId());
                setTagName(tag.getTagName());
            }});
            PCRepository.save(pc);
        }

        return new NewPostVO(
                postRepository.findByPostTitle(dto.getTitle(), userId).getPostId(),
                dto.getTitle()
        );
    }

    public NewCommentVO newComment(NewCommentDto dto, long userId, long postId) {
        Comments comment = new Comments();
        comment.setContent(dto.getComment());
        comment.setAuthor(userRepository.findByUserId(userId));
        comment.setCreatedAt(LocalDateTime.now());
        comment.setLikesCount(0);
        if (ObjectUtils.isEmpty(dto.getRepliedId())) {
            comment.setRepliedId((long) -1);
        }
        else {
            comment.setRepliedId(dto.getRepliedId());
        }
        comment.setPost(postRepository.findByPostId(postId));
        commentRepository.save(comment);

        // 更新帖子评论数
        Posts post = postRepository.findByPostId(postId);
        int commentsCount = post.getCommentsCount();
        post.setCommentsCount(commentsCount + 1);
        postRepository.save(post);

        return new NewCommentVO(
                postId,
                comment.getCommentId(),
                comment.getRepliedId()
        );
    }

    @Transactional
    public LikeVO Like(String type, long userId, long likeId) {
        boolean isLiked;
        if (type.equals("post")) {
            isLiked = postRepository.likedCheck(likeId, userId) > 0;
            Posts post = postRepository.findByPostId(likeId);
            if (isLiked) {
                postRepository.deleteByPostIdAndUserIdForLike(likeId, userId);
                post.setLikesCount(post.getLikesCount() - 1);
                isLiked = false;
            }
            else {
                postRepository.insert_like(likeId, userId);
                post.setLikesCount(post.getLikesCount() + 1);
                isLiked = true;
            }
            postRepository.save(post);
        }
        else if (type.equals("comment")) {
            isLiked = commentRepository.likedCheck(likeId, userId) > 0;
            Comments comment = commentRepository.findByCommentId(likeId);
            if (isLiked) {
                commentRepository.deleteByCommentIdAndUserId(likeId, userId);
                comment.setLikesCount(comment.getLikesCount() - 1);
                isLiked = false;
            }
            else {
                commentRepository.insert(likeId, userId);
                comment.setLikesCount(comment.getLikesCount() + 1);
                isLiked = true;
            }
            commentRepository.save(comment);
        }

        else
            return null;

        return new LikeVO(
                type,
                isLiked,
                likeId
        );
    }

    @Transactional
    public FavoriteVO Favorite(long postId, long userId) {
        boolean isFavorite;
        isFavorite = postRepository.favoriteCheck(postId, userId) > 0;
        Posts post = postRepository.findByPostId(postId);
        if (isFavorite) {
            postRepository.deleteByPostIdAndUserIdForFavorite(postId, userId);
            post.setFavoritesCount(post.getFavoritesCount() - 1);
            isFavorite = false;
        }
        else {
            postRepository.insert_favorite(postId, userId);
            post.setFavoritesCount(post.getFavoritesCount() + 1);
            isFavorite = true;
        }
        postRepository.save(post);

        return new FavoriteVO(
                isFavorite,
                postId
        );
    }
}
