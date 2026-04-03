package ben.qihuiarticle.util;

import ben.qihuiarticle.entity.entity_user.LPJsonFormat;
import com.alibaba.fastjson2.JSON;

import java.util.List;

public class jsonUtil {
    static public List<LPJsonFormat> LPtoJSON(String jsonText) {
        return JSON.parseArray(jsonText, LPJsonFormat.class);
    }
}
