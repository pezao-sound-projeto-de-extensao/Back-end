package sound.pezao.backend.mensageria;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sound.pezao.backend.config.RabbitMQConfig;
import sound.pezao.backend.mensageria.email.EmailMensagem;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para PublicadorDeMensagens")
class PublicadorDeMensagensTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PublicadorDeMensagens publicador;

    private final EmailMensagem mensagem = new EmailMensagem("ana@email.com", "Assunto", "Corpo");

    @AfterEach
    void encerrarTransacaoSimulada() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("Fora de transação, deve enviar na hora")
    void deveEnviarNaHoraForaDeTransacao() {
        publicador.publicar(RabbitMQConfig.FILA_EMAIL, mensagem);

        verify(rabbitTemplate).convertAndSend(RabbitMQConfig.FILA_EMAIL, mensagem);
    }

    @Test
    @DisplayName("Dentro de transação, deve esperar o commit para enviar")
    void deveEsperarOCommitParaEnviar() {
        TransactionSynchronizationManager.initSynchronization();

        publicador.publicar(RabbitMQConfig.FILA_EMAIL, mensagem);

        verifyNoInteractions(rabbitTemplate);

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

        verify(rabbitTemplate).convertAndSend(RabbitMQConfig.FILA_EMAIL, mensagem);
    }

    @Test
    @DisplayName("Não deve enviar nada se a transação for desfeita")
    void naoDeveEnviarQuandoATransacaoEDesfeita() {
        TransactionSynchronizationManager.initSynchronization();

        publicador.publicar(RabbitMQConfig.FILA_EMAIL, mensagem);

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verifyNoInteractions(rabbitTemplate);
    }
}
