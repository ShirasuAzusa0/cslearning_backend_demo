package ben.qihuipost.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeVO {
    private String type;
    private boolean isLiked;
    private long likeId;
}
