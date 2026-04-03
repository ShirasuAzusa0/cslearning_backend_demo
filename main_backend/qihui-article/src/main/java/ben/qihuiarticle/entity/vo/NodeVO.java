package ben.qihuiarticle.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeVO {
    private String name;
    private String info;
    private int level;
    private String type1;
    private String type2;
}
