package ben.qihuiai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizVO {
    private String question;
    private List<String> options;
    private String type;
    private String correct_answer;
    private String difficulty;
    private String knowledge_point;
    private String scoring_rules;
}
