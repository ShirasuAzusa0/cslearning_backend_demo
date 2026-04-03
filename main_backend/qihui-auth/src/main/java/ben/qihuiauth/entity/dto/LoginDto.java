package ben.qihuiauth.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
    private String account;
    private String password;
    private String captcha;
    private String captchaKey;
}
