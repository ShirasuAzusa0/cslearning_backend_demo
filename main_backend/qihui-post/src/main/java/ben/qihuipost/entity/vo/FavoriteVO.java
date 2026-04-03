package ben.qihuipost.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FavoriteVO {
    private boolean isFavorite;
    private long postId;
}
