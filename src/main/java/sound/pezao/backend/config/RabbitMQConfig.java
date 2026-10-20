package sound.pezao.backend.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Filas de e-mail. Segue o mesmo desenho da fila de imagens (ImagemItemProcessor):
 * as mensagens vão direto para a fila pelo nome, e a fila de erro (DLQ) recebe
 * o que esgotou as tentativas, em vez de a mensagem se perder ou entrar em loop.
 *
 * As filas são criadas pela aplicação ao conectar; nada precisa ser criado
 * manualmente no painel do RabbitMQ.
 */
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

    /**
     * Mensagens de objeto trafegam como JSON, legível no painel do RabbitMQ.
     * Não afeta a fila de imagens, que envia e recebe o Message cru com os bytes.
     */
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
