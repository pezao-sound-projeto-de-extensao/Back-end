package sound.pezao.backend.mensageria;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Envia mensagens de objeto (convertidas para JSON) para uma fila.
 *
 * Quando chamado dentro de uma transação, a mensagem só é enviada depois do
 * commit. Sem isso, o consumidor poderia processar algo de uma transação que
 * acabou desfeita: por exemplo, mandar o e-mail com um token que nunca foi salvo.
 */
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
