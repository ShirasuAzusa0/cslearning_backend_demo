package ben.qihuiauth.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewAdminDto {
    private String userName;
    private String email;
    private String password;
}
