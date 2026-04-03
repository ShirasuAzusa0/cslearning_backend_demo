package ben.qihuiarticle.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ArticleVO {
    private String articleName;
    private String articleContent;
    private List<NodeRelVO> articleKnowledgeGraph;
}
