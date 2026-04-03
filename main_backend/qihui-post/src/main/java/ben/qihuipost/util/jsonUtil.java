package ben.qihuipost.util;

import ben.qihuipost.entity.vo.NodeRelVO;
import com.alibaba.fastjson2.JSON;

import java.util.List;

public class jsonUtil {
    static public List<NodeRelVO> LPtoJSON(String jsonText) {
        return JSON.parseArray(jsonText, NodeRelVO.class);
    }
}
