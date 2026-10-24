package sound.pezao.backend.mensageria;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class PublicadorDeMensagens {

    private final RabbitTemplate rabbitTemplate;

    public PublicadorDeMensagens(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(String fila, Object mensagem) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(fila, mensagem);
                }
            });
            return;
        }

        enviar(fila, mensagem);
    }

    private void enviar(String fila, Object mensagem) {
        rabbitTemplate.convertAndSend(fila, mensagem);
    }
}
