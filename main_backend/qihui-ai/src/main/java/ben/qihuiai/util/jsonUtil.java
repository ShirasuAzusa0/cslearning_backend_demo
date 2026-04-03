package ben.qihuiai.util;

import ben.qihuiai.entity.vo.NodeRelVO;
import ben.qihuiai.entity.vo.ReferenceDataVO;
import ben.qihuiai.entity.vo.VideoVO;
import com.alibaba.fastjson2.JSON;

import java.util.List;

public class jsonUtil {
    public static VideoVO VideoToJSON(String jsonText) {
        return JSON.parseObject(jsonText, VideoVO.class);
    }

    public static List<NodeRelVO> NodeRelToJSON(String jsonText) {
        return JSON.parseArray(jsonText, NodeRelVO.class);
    }

    public static List<ReferenceDataVO> ReferenceDataTOJSON(String jsonText) {
        return JSON.parseArray(jsonText, ReferenceDataVO.class);
    }
}
