package ben.qihuiauth.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto extends NewAdminDto {
    private String userName;
    private String email;
    private String password;
    private String captcha;
    private String captchaKey;
}
