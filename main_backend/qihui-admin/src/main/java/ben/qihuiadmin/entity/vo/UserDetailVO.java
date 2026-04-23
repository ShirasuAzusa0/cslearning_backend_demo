package ben.qihuiadmin.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailVO {
    private Long userId;
    private String userName;
    private String email;
    private String type;
    private int replies;
    private int topics;
    private int followers;
    private int following;
    private String selfDescription;
    private LocalDateTime lastConnectedDate;
    private String avatarURL;
    private List<NodeRelVO> learningPath;
    private String learningPathDescription;
}
