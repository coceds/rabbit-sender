package capstone.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.util.logging.Logger;

@SpringBootApplication
public class Sender {

    private static final Logger logger = Logger.getLogger("Sender");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    RabbitTemplate template;

    public static void main(String[] args) {
        SpringApplication.run(Sender.class, args);
    }

    @PostConstruct
    public void send() throws Exception {
        final RateLimiter rateLimiter = RateLimiter.create(5.0);
        for (int id = 0; id < 100000; id++) {
            rateLimiter.acquire();
            byte[] payload = MAPPER.writeValueAsBytes(new Order(id, "TEST" + id, OrderType.values()[(id % 2)]));
            template.convertAndSend("example_key", payload);
            logger.info("Sent: " + id);
        }
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setAddresses("192.168.99.100:30000,192.168.99.100:30002,,192.168.99.100:30004");
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate template() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setExchange("simpleexample");
        return rabbitTemplate;
    }
}
