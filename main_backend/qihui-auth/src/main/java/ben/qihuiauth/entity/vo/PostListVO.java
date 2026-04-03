package ben.qihuiauth.entity.vo;

import ben.qihuiauth.entity.entity_post.Posts;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PostListVO {
    private List<PostProfileVO> posts;

    static public PostListVO getPostListVO(List<Posts> postList) {
        if (postList == null || postList.isEmpty()) return null;

        List<PostProfileVO> vos = postList.stream()
                .map(ps -> {
                    LastCommentedUserVO lastCommentedUserVO = null;
                    if (ps.getLastCommentedUser() != null) {
                        lastCommentedUserVO = new LastCommentedUserVO(
                                ps.getLastCommentedUser().getUserId(),
                                ps.getLastCommentedUser().getUserName()
                        );
                    }
                    return new PostProfileVO(
                            ps.getPostId(),
                            ps.getTitle(),

                            // author
                            new AuthorVO(
                                    ps.getAuthor().getUserId(),
                                    new AuthorVO.Attributes(
                                            ps.getAuthor().getAvatarURL(),
                                            ps.getAuthor().getUserName(),
                                            ps.getAuthor().getEmail()
                                    )
                            ),

                            // tags
                            ps.getTags().stream()
                                    .map(tag -> new TagVO(
                                            tag.getTag().getTagId(),
                                            tag.getTag().getTagName()
                                    )).toList(),

                            ps.getCreatedAt(),
                            ps.getLastCommentedAt(),

                            // commentUser
                            lastCommentedUserVO,

                            ps.getLikesCount(),
                            ps.getFavoritesCount(),
                            ps.getCommentsCount()
                    );
                }).toList();
        return new PostListVO(vos);
    }
}
