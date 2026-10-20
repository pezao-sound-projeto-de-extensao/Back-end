package sound.pezao.backend.mensageria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sound.pezao.backend.mensageria.email.EmailConsumer;
import sound.pezao.backend.mensageria.email.EmailMensagem;
import sound.pezao.backend.service.EnvioEmailService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do consumidor da fila de e-mail")
class ConsumidoresTest {

    @Mock
    private EnvioEmailService envioEmailService;

    @Test
    @DisplayName("Consumidor de e-mail deve repassar a mensagem para o envio")
    void consumidorDeEmailDeveEnviar() {
        EmailMensagem mensagem = new EmailMensagem("ana@email.com", "Assunto", "Corpo");

        new EmailConsumer(envioEmailService).consumir(mensagem);

        verify(envioEmailService).enviar(mensagem);
    }
}
