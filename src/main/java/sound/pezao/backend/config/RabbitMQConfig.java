package sound.pezao.backend.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_EMAIL = "emails.enviar";
    public static final String FILA_EMAIL_DLQ = FILA_EMAIL + ".dlq";

    @Bean
    public Queue filaEmail() {
        return QueueBuilder.durable(FILA_EMAIL)
                .deadLetterExchange("")
                .deadLetterRoutingKey(FILA_EMAIL_DLQ)
                .build();
    }

    @Bean
    public Queue filaEmailDlq() {
        return QueueBuilder.durable(FILA_EMAIL_DLQ).build();
    }

    @Bean
    public MessageConverter messageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setAssumeSupportedContentType(false);
        return converter;
    }
}
