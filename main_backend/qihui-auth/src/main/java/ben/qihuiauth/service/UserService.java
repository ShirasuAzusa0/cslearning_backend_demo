package ben.qihuiauth.service;

import ben.qihuiauth.entity.dto.RegisterDto;
import ben.qihuiauth.entity.entity_users.Users;
import ben.qihuiauth.entity.dto.UserEditDto;
import ben.qihuiauth.entity.entity_users.userType;
import ben.qihuiauth.entity.vo.AccountVO;
import ben.qihuiauth.repository.UserRepository;
import ben.qihuiauth.util.JwtUtils;
import ben.qihuiauth.util.RSAKeyUtils;
import com.alibaba.fastjson2.JSON;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Set;

import static ben.qihuiauth.util.JsonUtils.loadBKT;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final RSAKeyUtils rsaKeyUtil;

    public UserService(UserRepository userRepository,
                       JwtUtils jwtUtils,
                       PasswordEncoder passwordEncoder,
                       RSAKeyUtils rsaKeyUtil) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.rsaKeyUtil = rsaKeyUtil;
    }

    public AccountVO register(Users user) {
        userRepository.save(user);
        AccountVO vo = new AccountVO();

        // 生成 JWT 令牌
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
        UserDetails userDetails = User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(auth.getAuthorities())
                .build();

        String token = jwtUtils.generateJWT(userDetails, user.getUserId(), user.getUserName(), user.getAvatarURL());

        vo.setUserId(user.getUserId());

        String bearerToken = "Bearer " + token;
        vo.setUserId(user.getUserId());
        vo.setToken(bearerToken);
        vo.setToken(token);

        return vo;
    }

    public AccountVO login(Users user) {
        userRepository.save(user);

        // 生成 JWT 令牌
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
        UserDetails userDetails = User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(auth.getAuthorities())
                .build();

        String token = jwtUtils.generateJWT(userDetails, user.getUserId(), user.getUserName(), user.getAvatarURL());
        String bearerToken = "Bearer " + token;
        AccountVO vo = new AccountVO();
        vo.setUserId(user.getUserId());
        vo.setToken(bearerToken);

        return vo;
    }

    public String EditUserInfo(long userId, UserEditDto dto, MultipartFile avatarFile) {
        String avatarPath = null;

        // 本地保存用户头像图片文件
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String fileName = avatarFile.getOriginalFilename();
            if(fileName != null) {
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                Set<String> allowed = Set.of("png", "jpg", "jpeg", "gif");
                if (!allowed.contains(extension)) {
                    return "edit avatar fail";
                }
                try {
                    String uploadDir = System.getProperty("user.dir") + "/upload/avatar/";
                    File uploadDirFile = new File(uploadDir);
                    if (!uploadDirFile.exists()) {
                        uploadDirFile.mkdirs();
                    }
                    String savePath = uploadDir + fileName;
                    File file = new File(savePath);
                    avatarFile.transferTo(file);
                    avatarPath = "/upload/avatar/" + fileName;
                } catch (Exception e) {
                    return "edit avatar fail";
                }
            }
        }

        // 数据库更新
        // 对密码解密后再加密（RSA解密->BCrypt加密）
        String encryptedPassword = "";
        String passwordUpdateResult = null;

        // 判断是否需要更新密码
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            try {
                String rawPassword = dto.getPassword();
                // 替换空白字符、空格转回 + 号
                String fixedPassword = rawPassword.replace(' ', '+');
                String encryptedPasswordClean = fixedPassword.replaceAll("\\s", "");

                // 解密，再加密
                String decryptedPassword = rsaKeyUtil.decrypt(encryptedPasswordClean);
                encryptedPassword = passwordEncoder.encode(decryptedPassword);
                passwordUpdateResult = "password updated successfully";
            } catch (Exception e) {
                // 解密失败，返回错误
                return "password decryption failed";
            }
        }

        // 存储
        Users user = userRepository.findByUserId(userId);
        if (StringUtils.hasText(dto.getUserName())) user.setUserName(dto.getUserName());
        if (StringUtils.hasText(dto.getSelfDescription())) user.setSelfDescription(dto.getSelfDescription());
        if (avatarPath != null) user.setAvatarURL(avatarPath);
        if (passwordUpdateResult != null) user.setPassword(encryptedPassword);
        userRepository.save(user);

        return passwordUpdateResult != null ? passwordUpdateResult : "success";
    }

    public String passwordDecrypted(String rawPassword) {
        String fixedPassword = rawPassword.replace(' ', '+');
        String encryptedPasswordClean = fixedPassword.replaceAll("\\s+", "");
        return rsaKeyUtil.decrypt(encryptedPasswordClean);
    }

    public Users newAdmin(RegisterDto dto, String encryptedPassword) {
        return newUser(dto, encryptedPassword);
    }

    public Users newUser(RegisterDto dto, String encryptedPassword) {
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
        userRepository.save(user);
        return user;
    }
}
