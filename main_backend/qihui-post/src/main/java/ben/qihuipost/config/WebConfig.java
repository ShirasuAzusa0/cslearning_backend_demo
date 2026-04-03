package ben.qihuipost.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取当前运行目录
        String baseDir = System.getProperty("user.dir");

        // 拼接 upload/avatar
        File uploadDir = new File(baseDir, "upload/avatar");

        String location = "file:" + uploadDir.getAbsolutePath() + "/";

        registry.addResourceHandler("/upload/avatar/**")
                .addResourceLocations(location);
    }
}
