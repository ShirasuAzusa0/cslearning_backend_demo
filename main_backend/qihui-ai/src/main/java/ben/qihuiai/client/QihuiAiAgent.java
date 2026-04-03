package ben.qihuiai.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

// 专用于通过 Feign 实现跨后端数据传输功能的代理层
@FeignClient(name = "ai-agent-service")
public interface QihuiAiAgent {
    @PostMapping("/api/agent/v1/er/search")
    Map<String, Object> search(@RequestBody Map<String, Object> body);
}
