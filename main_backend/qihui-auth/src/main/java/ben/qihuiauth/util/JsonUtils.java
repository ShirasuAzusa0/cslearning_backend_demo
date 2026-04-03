package ben.qihuiauth.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class JsonUtils {

    public static Map<String, Object> loadBKT() {
        return loadJsonFromResources("BKTtable.json");
    }

    private static Map<String, Object> loadJsonFromResources(String fileName) {
        try (
                InputStream inputStream = JsonUtils.class
                        .getClassLoader()
                        .getResourceAsStream(fileName)
        ) {

            if (inputStream == null) {
                throw new RuntimeException("File not found in resources: " + fileName);
            }

            String jsonText = new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

            return JSON.parseObject(
                    jsonText,
                    new TypeReference<>() {
                    }
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON file: " + fileName, e);
        }
    }
}
