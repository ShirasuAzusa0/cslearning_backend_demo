package ben.qihuiai.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDeleteDto {
    private List<Long> documentIds;
    private int kbId;
}
