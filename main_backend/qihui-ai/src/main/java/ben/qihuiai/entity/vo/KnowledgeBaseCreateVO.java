package ben.qihuiai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KnowledgeBaseCreateVO {
    private int kbId;
    private String kbName;
    private String creator;
    private String kbDescription;
    private String embeddingModel;
    private String rerankerModel;
    private String kbType;
    private LocalDateTime createdAt;
}
