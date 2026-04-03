package ben.qihuiai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DocumentVO {
    private long documentId;
    private String documentName;
    private long documentSize;
    private int documentParts;
    private LocalDateTime documentLoadedAt;
    private String documentType;
    private String documentContent;
}
