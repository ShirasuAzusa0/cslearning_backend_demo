package ben.qihuiauth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Component
public class JwtUtils {
    // 加密密钥
    @Value("${spring.security.jwt.key}")
    private String key;

    // JWT 有效期设置（单位：天）
    @Value("${spring.security.jwt.expire}")
    private int expire;

    // JWT 令牌过期时间计算方法
    public Date expireTime() {
        Calendar cal = Calendar.getInstance();
        // 若配置异常则回退回默认的 7 天过期
        int days = Math.max(0, expire);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    // 创建 JWT 令牌，需要用到用户的信息（从 info 中提取出来）、id、用户名
    public String generateJWT(UserDetails userDetails, long userId, String userName, String avatarURL) {
        // 使用 HMAC256 加密算法
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expireTime();

        // 将 authorities 转为 String[] 并用 withArrayClaim 明确写入数组类型
        String[] authoritiesArray = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .toArray(String[]::new);

        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("userName", userName)
                .withClaim("avatarURL", avatarURL)
                .withArrayClaim("authorities", authoritiesArray)
                .withIssuedAt(new Date())
                .withExpiresAt(expire)
                .sign(algorithm);
    }
}
