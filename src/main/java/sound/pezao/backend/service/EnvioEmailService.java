package sound.pezao.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import sound.pezao.backend.mensageria.email.EmailMensagem;

@Service
public class EnvioEmailService {

    private final JavaMailSender mailSender;
    private final String remetente;

    public EnvioEmailService(
            JavaMailSender mailSender,
            @Value("${app.email.remetente}") String remetente
    ) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public void enviar(EmailMensagem mensagem) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(remetente);
        email.setTo(mensagem.destinatario());
        email.setSubject(mensagem.assunto());
        email.setText(mensagem.corpo());

        mailSender.send(email);
    }
}
