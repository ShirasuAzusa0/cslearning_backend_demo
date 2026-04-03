package ben.qihuipost.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewCommentVO {
    private long postId;
    private long commentId;
    private long replyId;
}
