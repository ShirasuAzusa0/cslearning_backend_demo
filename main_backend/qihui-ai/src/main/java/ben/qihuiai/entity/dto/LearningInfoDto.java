package ben.qihuiai.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearningInfoDto {
    private long userId;
    private String learningTarget;
    private int learningStage;
    private int availableTime;
}
