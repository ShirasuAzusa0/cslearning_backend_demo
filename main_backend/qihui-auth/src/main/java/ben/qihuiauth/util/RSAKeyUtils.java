package ben.qihuiauth.util;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RSAKeyUtils {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    // 生成 RSA 密钥对（2048位）
    public static KeyPair generateRSAKeyPair()  throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    // 将生成好的 key 的字节数组写成 Base64 并以 PEM 格式保存到文件中
    public static void writeKeyToFile(Path path, byte[] keyBytes, String header, String footer) throws IOException {
        String pem = header + "\n" +
                Base64.getMimeEncoder(64, "\n".getBytes())
                        .encodeToString(keyBytes) + "\n" +
                footer + "\n";

        Files.createDirectories(path.getParent());
        Files.writeString(path, pem);
    }

    // 从文件中读取 PEM 格式的密钥
    public static String readKeyFromFile(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    // 私钥解密
    public static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] plainText = cipher.doFinal(Base64.getMimeDecoder().decode(cipherText));
        return new String(plainText, StandardCharsets.UTF_8);
    }

    // 解密
    public String decrypt(String cipherText) {
        try {
            return decrypt(cipherText, privateKey);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }

    // 获取 Base64 格式的公钥
    public String getPublicKeyBase64() {
        if (publicKey == null) {
            throw new IllegalStateException("公钥未初始化");
        }
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    // 从 PEM 加载公钥
    public static PublicKey getPublicKey(String pem) throws Exception {
        String publicKeyPEM = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getMimeDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(keySpec);
    }

    // 从 PEM 加载私钥
    public static PrivateKey getPrivateKey(String pem) throws Exception {
        String privatePEM = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getMimeDecoder().decode(privatePEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    // 生成并保存密钥对到 key 目录
    public static void generateAndSaveKeyPair() throws Exception {
        // 获取项目根目录路径
        Path classPath = Path.of(RSAKeyUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        // target/classes/ 上两级是项目根目录
        Path projectRoot = classPath.getParent().getParent();
        Path keyDir = projectRoot
                .resolve("src")
                .resolve("main")
                .resolve("resources")
                .resolve("key");

        System.out.println("生成密钥对目录：" + keyDir.toAbsolutePath());

        KeyPair keyPair = generateRSAKeyPair();
        writeKeyToFile(keyDir.resolve("public_key.pem"),keyPair.getPublic().getEncoded(),
                "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");
        writeKeyToFile(keyDir.resolve("private_key.pem"), keyPair.getPrivate().getEncoded(),
                "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
    }

    @PostConstruct
    public void init() {
        try {
            Path classPath = Path.of(RSAKeyUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path projectRoot = classPath.getParent().getParent();
            Path keyDir = projectRoot
                    .resolve("src")
                    .resolve("main")
                    .resolve("resources")
                    .resolve("key");

            Path privateKeyPath = keyDir.resolve("private_key.pem");
            Path publicKeyPath = keyDir.resolve("public_key.pem");

            // 若密钥文件不存在，则自动生成
            if (!Files.exists(privateKeyPath) || !Files.exists(publicKeyPath)) {
                System.out.println("密钥文件不存在，开始生成...");
                generateAndSaveKeyPair();
                System.out.println("密钥生成完毕！");
            }

            // 读取私钥文件
            String pem = readKeyFromFile(privateKeyPath);
            this.privateKey = getPrivateKey(pem);

            // 读取公钥文件
            pem = readKeyFromFile(publicKeyPath);
            this.publicKey = getPublicKey(pem);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("初始化RSA密钥失败", e);
        }
    }
}
