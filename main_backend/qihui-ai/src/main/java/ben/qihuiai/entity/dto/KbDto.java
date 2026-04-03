package ben.qihuiai.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KbDto {
    private String kbName;
    private String kbDescription;
    private String embeddingModel;
    private String rerankerModel;
    private String kbType;
}
