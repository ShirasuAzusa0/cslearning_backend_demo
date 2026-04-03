package ben.qihuipost.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TagDetailVO {
    private int tagId;
    private String tagName;
    private int hueColor;
    private String description;
    private int postsCount;
    private LocalDateTime lastPostTime;
}
