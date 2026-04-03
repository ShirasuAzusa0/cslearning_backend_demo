package ben.qihuiauth.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LastCommentedUserVO {
    private long userId;
    private String userName;
}
