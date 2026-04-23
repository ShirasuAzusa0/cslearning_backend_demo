package ben.qihuiadmin.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PostDetailVO {
    private long postId;
    private String Title;
    private AuthorVO author;
    private List<TagVO> tags;
    private LocalDateTime createdAt;
    private LocalDateTime lastCommentedAt;
    private int commentsCount;
    private int likesCount;
    private int favoritesCount;
    private String content;
    private List<CommentVO> comments;
}
