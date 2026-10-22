package sound.pezao.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sound.pezao.backend.config.RabbitMQConfig;
import sound.pezao.backend.entities.TokenRecuperacaoSenha;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.mensageria.PublicadorDeMensagens;
import sound.pezao.backend.mensageria.email.EmailMensagem;
import sound.pezao.backend.repository.RefreshTokenRepository;
import sound.pezao.backend.repository.TokenRecuperacaoSenhaRepository;
import sound.pezao.backend.repository.UsuarioRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Fluxo de "esqueci minha senha".
 *
 * 1. solicitar: gera um token de uso único e publica o e-mail com o link na fila.
 * 2. redefinir: valida o token, troca a senha e encerra as sessões abertas.
 */
@Service
public class RecuperacaoSenhaService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacaoSenhaService.class);

    static final String ASSUNTO = "StockFlow - Redefinição de senha";
    static final String MENSAGEM_TOKEN_INVALIDO = "Link de redefinição inválido ou expirado.";

    /** Intervalo mínimo entre dois e-mails para o mesmo usuário, contra spam na caixa de entrada. */
    static final Duration INTERVALO_MINIMO_ENTRE_PEDIDOS = Duration.ofMinutes(1);

    /** Usado quando o CORS libera qualquer origem ("*"), como no padrão de desenvolvimento. */
    static final String FRONT_PADRAO = "http://localhost:5173";

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacaoSenhaRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PublicadorDeMensagens publicador;
    private final String frontendUrl;
    private final Duration validade;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * @param origensDoCors a mesma variável CORS que libera o front na API. O link do
     *                      e-mail aponta para o primeiro endereço dela.
     */
    public RecuperacaoSenhaService(
            UsuarioRepository usuarioRepository,
            TokenRecuperacaoSenhaRepository tokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            PublicadorDeMensagens publicador,
            @Value("${app.cors.allowed-origins:*}") String origensDoCors,
            @Value("${app.recuperacao-senha.validade}") Duration validade
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.publicador = publicador;
        this.frontendUrl = enderecoDoFront(origensDoCors);
        this.validade = validade;
    }

    /**
     * Converte o valor do CORS em um endereço que funcione como link de e-mail.
     * O CORS pode listar várias origens separadas por vírgula, pode ser "*", e em
     * produção chega sem protocolo (só o DNS do load balancer). Sem "http://" muitos
     * clientes de e-mail não transformam o texto em link clicável.
     */
    static String enderecoDoFront(String origensDoCors) {
        String primeira = origensDoCors == null ? "" : origensDoCors.split(",")[0].trim();

        if (primeira.isEmpty() || primeira.equals("*")) {
            return FRONT_PADRAO;
        }
        if (!primeira.startsWith("http://") && !primeira.startsWith("https://")) {
            primeira = "http://" + primeira;
        }
        return primeira.endsWith("/") ? primeira.substring(0, primeira.length() - 1) : primeira;
    }

    /**
     * Não informa se o e-mail existe: a resposta da API é a mesma nos dois casos,
     * para que ninguém use esta rota para descobrir quem tem conta no sistema.
     */
    @Transactional
    public void solicitar(String email) {
        Optional<Usuario> encontrado = usuarioRepository.findByEmailIgnoreCase(email);

        if (encontrado.isEmpty() || !encontrado.get().isAtivo()) {
            log.info("Recuperação de senha solicitada para e-mail sem conta ativa.");
            return;
        }

        Usuario usuario = encontrado.get();
        Instant agora = Instant.now();

        if (tokenRepository.existsByUsuario_IdAndCriadoEmAfter(usuario.getId(), agora.minus(INTERVALO_MINIMO_ENTRE_PEDIDOS))) {
            log.info("Recuperação de senha ignorada: pedido repetido em menos de um minuto.");
            return;
        }

        tokenRepository.invalidarPendentesDoUsuario(usuario.getId(), agora);

        String segredo = gerarSegredo();
        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
        token.setUsuario(usuario);
        token.setTokenHash(passwordEncoder.encode(segredo));
        token.setExpiraEm(agora.plus(validade));
        TokenRecuperacaoSenha salvo = tokenRepository.save(token);

        String link = frontendUrl + "/redefinir-senha?token=" + salvo.getId() + "." + segredo;

        publicador.publicar(
                RabbitMQConfig.FILA_EMAIL,
                new EmailMensagem(usuario.getEmail(), ASSUNTO, montarCorpo(usuario.getNome(), link))
        );
    }

    @Transactional
    public void redefinir(String token, String senhaNova) {
        TokenRecuperacaoSenha registro = buscarValido(token);
        Usuario usuario = registro.getUsuario();
        Instant agora = Instant.now();

        usuario.setSenhaHash(passwordEncoder.encode(senhaNova));
        usuarioRepository.save(usuario);

        registro.setUsadoEm(agora);
        refreshTokenRepository.revogarTodosDoUsuario(usuario.getId(), agora);
    }

    private TokenRecuperacaoSenha buscarValido(String token) {
        String[] partes = token == null ? new String[0] : token.split("\\.", 2);

        if (partes.length != 2) {
            throw new IllegalArgumentException(MENSAGEM_TOKEN_INVALIDO);
        }

        long tokenId;
        try {
            tokenId = Long.parseLong(partes[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(MENSAGEM_TOKEN_INVALIDO);
        }

        return tokenRepository.findByIdAndUsadoEmIsNull(tokenId)
                .filter(TokenRecuperacaoSenha::isValido)
                .filter(t -> passwordEncoder.matches(partes[1], t.getTokenHash()))
                .orElseThrow(() -> new IllegalArgumentException(MENSAGEM_TOKEN_INVALIDO));
    }

    private String montarCorpo(String nome, String link) {
        return """
                Olá, %s!

                Recebemos um pedido para redefinir a senha da sua conta no StockFlow.
                Para criar uma nova senha, acesse o link abaixo (válido por %d minutos):

                %s

                Se você não fez esse pedido, ignore este e-mail: sua senha atual continua valendo.
                """.formatted(nome, validade.toMinutes(), link);
    }

    private String gerarSegredo() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
