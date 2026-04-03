package ben.qihuipost.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ProfileVO {
    private long userId;
    private String userName;
    private String email;
    private String avatarURL;
    private String selfDescription;
    private LocalDateTime lastLoginDate;
    private Counts counts;
    private List<NodeRelVO> learningPath;
    private String learnPathDescription;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class Counts {
        private int replies;
        private int topics;
        private int followers;
        private int following;
    }
}
