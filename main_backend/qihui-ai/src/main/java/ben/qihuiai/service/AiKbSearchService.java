package ben.qihuiai.service;

import ben.qihuiai.client.QihuiAiAgent;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiKbSearchService {
    private final QihuiAiAgent qihuiAiAgent;

    public AiKbSearchService(QihuiAiAgent qihuiAiAgent) {
        this.qihuiAiAgent = qihuiAiAgent;
    }

    public Map<String, Object> search(String query, Integer kbId) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query不能为空");
        }

        if (kbId == null) {
            throw new IllegalArgumentException("kbId不能为空");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        body.put("kbId", kbId);

        return qihuiAiAgent.search(body);
    }
}
