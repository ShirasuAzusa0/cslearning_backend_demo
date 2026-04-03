package ben.qihuigateway.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {
    // 加密密钥
    @Value("${spring.security.jwt.key}")
    private String key;

    // JWT 令牌验证与解析方法
    public DecodedJWT verifyJWT(String headerToken) {
        // 去前缀并校验
        String token = this.convertToken(headerToken);
        if (token == null) return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = verifier.verify(token);
            Date expiresAt = jwt.getExpiresAt();
            if (expiresAt != null)
                if (new Date().after(expiresAt))
                    return null;

            return jwt;
        } catch (JWTVerificationException e) {
            System.out.println("JWT verification failed: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("JWT verify unexpected error: " + e.getMessage());
            return null;
        }
    }

    // 格式化 token，将 headerToken 的前缀去掉
    private String convertToken(String headerToken) {
        if (headerToken == null) return null;
        String trimmed = headerToken.trim();
        if(!trimmed.startsWith("Bearer "))
            return null;

        // 返回去掉前缀并 trim 的结果
        return trimmed.substring(7);
    }
}
