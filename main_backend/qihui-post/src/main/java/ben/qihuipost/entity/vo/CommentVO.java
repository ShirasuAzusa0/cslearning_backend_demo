package ben.qihuipost.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentVO {
    private long commentId;
    private AuthorVO author;
    private String content;
    private LocalDateTime createdAt;
    private int likesCount;
    private boolean isLiked;
    private Long repliedId;
}
