package ben.qihuiai.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String VECTOR_QUEUE = "vectorization_queue";

    @Bean
    public Queue vectorizationQueue() {
        return new Queue(VECTOR_QUEUE, true);
    }
}
