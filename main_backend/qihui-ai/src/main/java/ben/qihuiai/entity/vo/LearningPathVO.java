package ben.qihuiai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LearningPathVO {
    private List<NodeRelVO> finalPath;
    private String explanation;
}
