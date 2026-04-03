package ben.qihuiai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DocumentProfileVO {
    private long documentId;
    private String documentName;
    private long documentSize;
    private int documentParts;
    private LocalDateTime documentLoadedAt;
}
