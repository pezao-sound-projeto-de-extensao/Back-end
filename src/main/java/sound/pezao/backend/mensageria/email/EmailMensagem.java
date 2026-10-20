package sound.pezao.backend.mensageria.email;

/**
 * Conteúdo de um e-mail a ser enviado em segundo plano.
 * Genérico de propósito: qualquer funcionalidade que precise mandar e-mail
 * (recuperação de senha hoje, orçamento para o cliente depois) usa a mesma fila.
 */
public record EmailMensagem(
        String destinatario,
        String assunto,
        String corpo
) {
}
