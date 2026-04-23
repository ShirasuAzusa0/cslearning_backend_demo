package ben.qihuiadmin.util;

import ben.qihuiadmin.entity.vo.NodeRelVO;
import com.alibaba.fastjson2.JSON;

import java.util.List;

public class jsonUtil {
    public static List<NodeRelVO> NodeRelToJSON(String jsonText) {
        return JSON.parseArray(jsonText, NodeRelVO.class);
    }
}
