package ben.qihuiai.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizResultListDto {
    private long userId;
    private String target;
    private String tend;
    private List<QuizResultDto> quizResults;
}
