package ben.qihuiai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SessionListElementVO {
    private int sessionId;
    private String sessionName;
    private String modelName;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
}
