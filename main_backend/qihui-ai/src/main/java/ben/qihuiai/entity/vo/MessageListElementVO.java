package ben.qihuiai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class MessageListElementVO {
    private long messageId;
    private String content;
    private String role;
    private LocalDateTime createdAt;
    private List<ReferenceDataVO> referenceData;
}
