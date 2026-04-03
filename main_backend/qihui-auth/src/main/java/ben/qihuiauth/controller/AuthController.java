package ben.qihuiauth.controller;

import ben.qihuiauth.entity.RestBean;
import ben.qihuiauth.entity.dto.LoginDto;
import ben.qihuiauth.entity.dto.RegisterDto;
import ben.qihuiauth.entity.entity_users.Users;
import ben.qihuiauth.entity.dto.UserEditDto;
import ben.qihuiauth.entity.entity_users.userType;
import ben.qihuiauth.entity.vo.AccountVO;
import ben.qihuiauth.repository.UserRepository;
import ben.qihuiauth.service.UserService;
import ben.qihuiauth.util.RSAKeyUtils;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;

import static ben.qihuiauth.util.JsonUtils.loadBKT;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RSAKeyUtils rsaKeyUtil;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    final StringRedisTemplate redisTemplate;

    public AuthController(UserService userService,
                          PasswordEncoder passwordEncoder,
                          StringRedisTemplate redisTemplate) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    // 验证码验证
    private ResponseEntity<?> verifyCaptcha(String captchaKey, String captchaInput) {
        if (captchaKey == null || captchaInput == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("验证码不能为空"));
        }
        System.out.println(captchaKey);
        // 从 Redis 获取验证码
        String redisCaptcha = redisTemplate.opsForValue().get("captcha:" + captchaKey);
        System.out.println(redisCaptcha);
        if (redisCaptcha == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("验证码已过期"));
        }

        if (!redisCaptcha.equalsIgnoreCase(captchaInput.trim())) {
            return ResponseEntity.badRequest().body(RestBean.failure("验证码错误"));
        }

        // 验证通过后删除验证码，防止重复利用
        redisTemplate.delete("captcha:" + captchaKey);
        return null;
    }

    // 注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto dto) {
        if (userRepository.findByEmail(dto.getEmail()) != null) {
            return ResponseEntity.badRequest().body(RestBean.failure("该邮箱已被注册"));
        }

        // 校验验证码
        String captchaKey = dto.getCaptchaKey();
        String captchaInput = dto.getCaptcha();
        ResponseEntity<?> captchaResult = verifyCaptcha(captchaKey, captchaInput);
        // 验证失败则直接返回
        if (captchaResult != null) {
            return captchaResult;
        }

        // 对密码进行解密后再加密（RSA -> BCrypt）
        String encryptedPassword = null;
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            String rawPassword = dto.getPassword();
            // 替换空白字符、空格转回 + 号
            String fixedPassword = rawPassword.replace(' ', '+');
            String encryptedPasswordClean = fixedPassword.replaceAll("\\s", "");

            System.out.println(encryptedPasswordClean);

            // 解密
            String decryptedPassword = rsaKeyUtil.decrypt(encryptedPasswordClean);
            // 加密存储
            encryptedPassword = passwordEncoder.encode(decryptedPassword);
        }

        if (encryptedPassword == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("密码加密存储失败"));
        }

        Users user = new Users();
        user.setUserName(dto.getUserName());
        user.setPassword(encryptedPassword);
        user.setEmail(dto.getEmail());
        user.setAvatarURL("https://avatars.githubusercontent.com/u/19370775");
        user.setSelfDescription("这个用户很懒,什么都没有留下");
        user.setReplies(0);
        user.setTopics(0);
        user.setFollower(0);
        user.setFollowing(0);
        user.setLastConnectedDate(LocalDateTime.now());
        user.setType(userType.user);
        user.setBktTable(JSON.toJSONString(loadBKT()));

        AccountVO vo = userService.register(user);

        return ResponseEntity.ok(RestBean.successType1("注册成功", vo));
    }

    // 登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        if (userRepository.findByEmail(dto.getAccount()) == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("不存在该账号"));
        }

        // 校验验证码
        String captchaKey = dto.getCaptchaKey();
        String captchaInput = dto.getCaptcha();
        ResponseEntity<?> captchaResult = verifyCaptcha(captchaKey, captchaInput);
        if (captchaResult != null) {
            return captchaResult;
        }

        String email = dto.getAccount();

        Users user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("该邮箱未注册账号"));
        }

        // 对密码进行解密
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            String rawPassword = dto.getPassword();
            String fixedPassword = rawPassword.replace(' ', '+');
            String encryptedPasswordClean = fixedPassword.replaceAll("\\s+", "");

            System.out.println(encryptedPasswordClean);

            String decryptedPassword = rsaKeyUtil.decrypt(encryptedPasswordClean);

            // 使用BCrypt验证密码
            if (!passwordEncoder.matches(decryptedPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body(RestBean.failure("密码错误"));
            }
        }
        else return ResponseEntity.badRequest().body(RestBean.failure("密码不能为空"));

        user.setLastConnectedDate(LocalDateTime.now());

        AccountVO vo = userService.login(user);

        return ResponseEntity.ok(RestBean.successType1("登录成功", vo));
    }

    // 管理员登录
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginDto dto) {
        if (userRepository.findByEmail(dto.getAccount()) == null || userRepository.findByEmail(dto.getAccount()).getType() != userType.admin) {
            return ResponseEntity.badRequest().body(RestBean.failure("不存在该账号"));
        }

        // 校验验证码
        String captchaKey = dto.getCaptchaKey();
        String captchaInput = dto.getCaptcha();
        ResponseEntity<?> captchaResult = verifyCaptcha(captchaKey, captchaInput);
        if (captchaResult != null) {
            return captchaResult;
        }

        String email = dto.getAccount();

        Users user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(RestBean.failure("该邮箱未注册账号"));
        }

        // 对密码进行解密
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            String rawPassword = dto.getPassword();
            String fixedPassword = rawPassword.replace(' ', '+');
            String encryptedPasswordClean = fixedPassword.replaceAll("\\s+", "");

            System.out.println(encryptedPasswordClean);

            String decryptedPassword = rsaKeyUtil.decrypt(encryptedPasswordClean);

            // 使用BCrypt验证密码
            if (!passwordEncoder.matches(decryptedPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body(RestBean.failure("密码错误"));
            }
        }
        else return ResponseEntity.badRequest().body(RestBean.failure("密码不能为空"));

        user.setLastConnectedDate(LocalDateTime.now());

        AccountVO vo = userService.login(user);

        return ResponseEntity.ok(RestBean.successType1("管理员登录成功", vo));
    }

    // 修改用户个人信息
    // 通过 consumes = MediaType.MULTIPART_FORM_DATA_VALUE 告知 Spring 本接口只接受 multipart/form-data 请求
    @PutMapping(value = "/edit/{userId}")
    public ResponseEntity<?> updateUserInfo(@PathVariable(name = "userId") long userId,
                                            @RequestParam(name = "userName", required = false) String userName,
                                            @RequestParam(name = "selfDescription", required = false) String selfDescription,
                                            @RequestParam(name = "password", required = false) String password,
                                            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile
    ) {
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(RestBean.failure("userId不能为空"));
        }

        UserEditDto dto = new UserEditDto(
                userName,
                selfDescription,
                password
        );

        String res = userService.EditUserInfo(userId, dto, avatarFile);

        if (Objects.equals(res, "edit avatar fail")) {
            return ResponseEntity.badRequest().body(RestBean.failure("头像修改失败"));
        }

        else if (Objects.equals(res, "edit password fail")) {
            return ResponseEntity.badRequest().body(RestBean.failure("密码修改失败"));
        }

        return ResponseEntity.ok(RestBean.successType2("用户信息修改成功"));
    }
}
