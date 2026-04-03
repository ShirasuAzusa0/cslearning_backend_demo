package ben.qihuipost.entity.dto;

import ben.qihuipost.entity.vo.TagVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewPostDto {
    private String title;
    private List<TagVO> tags;
    private String content;
}
