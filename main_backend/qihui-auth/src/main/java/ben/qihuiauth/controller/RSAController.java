package ben.qihuiauth.controller;

import ben.qihuiauth.entity.RestBean;
import ben.qihuiauth.util.RSAKeyUtils;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class RSAController {
    @Resource
    private RSAKeyUtils rsaKeyUtils;

    // 若路径只有 RequestMapping 中的那一部分，则此处的请求类型 Mapping 不写 URL
    @GetMapping("/publicKey")
    public ResponseEntity<?> getPublicKey() {
        String key = rsaKeyUtils.getPublicKeyBase64();
        return ResponseEntity.ok(RestBean.successKey(key));
    }
}
