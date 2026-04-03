package ben.qihuiai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class KnowledgeBaseVO {
    private int kbId;
    private String kbName;
    private String kbDescription;
    private int kbDocNum;
    private String kbType;
    private LocalDateTime createdAt;
    private String embeddingModel;
    private String rerankerModel;
    private CreatorVO creator;
    private List<DocumentProfileVO> documents;
}
