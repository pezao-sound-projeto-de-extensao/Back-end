package sound.pezao.backend.mensageria.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import sound.pezao.backend.config.RabbitMQConfig;
import sound.pezao.backend.service.EnvioEmailService;

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
