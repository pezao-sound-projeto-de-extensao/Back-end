package sound.pezao.backend.mensageria.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import sound.pezao.backend.config.RabbitMQConfig;
import sound.pezao.backend.service.EnvioEmailService;

/**
 * Consome a fila de e-mails.
 *
 * Se o SMTP falhar, a exceção sobe e o Spring tenta de novo (configurado em
 * application.properties). Esgotadas as tentativas, a mensagem vai para a DLQ
 * stockflow.email.dlq. A DLQ não tem consumidor de propósito: a mensagem fica
 * parada lá, visível no painel do RabbitMQ, até alguém investigar.
 */
@Component
public class EmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);

    private final EnvioEmailService envioEmailService;

    public EmailConsumer(EnvioEmailService envioEmailService) {
        this.envioEmailService = envioEmailService;
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_EMAIL)
    public void consumir(EmailMensagem mensagem) {
        envioEmailService.enviar(mensagem);
        log.info("E-mail \"{}\" enviado.", mensagem.assunto());
    }
}
