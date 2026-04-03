package ben.qihuiai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MultiResourceVO {
    private List<ArticleProfileVO> articles;
    private List<VideoVO> videos;
}
