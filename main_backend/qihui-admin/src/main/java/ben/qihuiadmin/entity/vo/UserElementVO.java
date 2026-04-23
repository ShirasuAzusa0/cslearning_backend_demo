package ben.qihuiadmin.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserElementVO {
    private String userName;
    private String email;
    private LocalDateTime lastConnectedDate;
}
