package ben.qihuiai.service;

import ben.qihuiai.entity.dto.VectorizationDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static ben.qihuiai.config.RabbitConfig.VECTOR_QUEUE;

// 专用于通过 RabbitMQ 实现跨后端数据传输功能的服务层服务
@Service
public class CrossPublishService {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public CrossPublishService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper();
    }

    // 将 dtos 异步批量发送到 RabbitMQ 队列
    public void publishVectorizationTasks(List<VectorizationDto> dtos) {
        try {
            String message = objectMapper.writeValueAsString(dtos);
            rabbitTemplate.convertAndSend(VECTOR_QUEUE, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("VectorizationDto 序列化失败", e);
        }
    }
}
