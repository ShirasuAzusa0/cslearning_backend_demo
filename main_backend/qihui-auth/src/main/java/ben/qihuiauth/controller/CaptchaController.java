package ben.qihuiauth.controller;

import ben.qihuiauth.entity.RestBean;
import ben.qihuiauth.entity.vo.CaptchaVO;
import ben.qihuiauth.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class CaptchaController {
    private final CaptchaService captchaService;
    private final StringRedisTemplate redisTemplate;

    // 使用构造器注入（避免 NPE）
    @Autowired
    public CaptchaController(CaptchaService captchaService, StringRedisTemplate redisTemplate) {
        this.captchaService = captchaService;
        this.redisTemplate = redisTemplate;
    }

    // 获取验证码图片传给前端
    @GetMapping("/captcha")
    public ResponseEntity<?> getCaptcha() {
        // 生成验证码文本
        String captchaText = captchaService.generateText();

        // 生成唯一的 key
        String key = UUID.randomUUID().toString();

        // 将验证码存入 Redis，1min有效
        redisTemplate.opsForValue().set("captcha:" + key, captchaText.toLowerCase(), 10, TimeUnit.MINUTES);

        // 生成 Base64 图片字符串
        String base64Image = captchaService.generateImageBase64(captchaText);

        CaptchaVO vo = new CaptchaVO(
                key,
                base64Image
        );

        return ResponseEntity.ok(RestBean.successType1("验证码获取成功", vo));
    }
}
