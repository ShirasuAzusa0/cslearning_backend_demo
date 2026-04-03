package ben.qihuiauth.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PostProfileVO {
    private long PostId;
    private String Title;
    private AuthorVO author;
    private List<TagVO> tags;
    private LocalDateTime createdAt;
    private LocalDateTime lastCommentedAt;
    private LastCommentedUserVO lastCommentedUser;
    private int likesCount;
    private int favoritesCount;
    private int commentsCount;
}
