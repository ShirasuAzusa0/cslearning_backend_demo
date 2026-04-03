package ben.qihuiai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RuleElementVO {
    private int ruleId;
    private String ruleName;
    private String description;
}
