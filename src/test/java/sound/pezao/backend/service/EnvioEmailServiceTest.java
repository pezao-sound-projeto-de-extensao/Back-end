package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import sound.pezao.backend.mensageria.email.EmailMensagem;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para EnvioEmailService")
class EnvioEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    @DisplayName("Deve montar o e-mail com remetente, destinatário, assunto e corpo")
    void deveMontarEEnviarOEmail() {
        EnvioEmailService service = new EnvioEmailService(mailSender, "nao-responda@stockflow.local");

        service.enviar(new EmailMensagem("ana@email.com", "Assunto", "Corpo do e-mail"));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage enviado = captor.getValue();

        assertEquals("nao-responda@stockflow.local", enviado.getFrom());
        assertArrayEquals(new String[]{"ana@email.com"}, enviado.getTo());
        assertEquals("Assunto", enviado.getSubject());
        assertEquals("Corpo do e-mail", enviado.getText());
    }

    @Test
    @DisplayName("Deve deixar a falha do SMTP subir, para a fila tentar de novo")
    void deveDeixarAFalhaDoSmtpSubir() {
        EnvioEmailService service = new EnvioEmailService(mailSender, "nao-responda@stockflow.local");
        doThrow(new MailSendException("SMTP fora do ar")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(MailSendException.class,
                () -> service.enviar(new EmailMensagem("ana@email.com", "Assunto", "Corpo")));
    }
}
