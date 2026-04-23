package ben.qihuiadmin.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileVO {
    private Long userId;
    private String userName;
    private String email;
    private String type;
    private int replies;
    private int topics;
    private int followers;
    private int following;
    private LocalDateTime lastConnectedDate;
}
