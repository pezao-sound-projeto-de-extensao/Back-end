package sound.pezao.backend.mensageria.email;

public record EmailMensagem(
        String destinatario,
        String assunto,
        String corpo
) {
}
