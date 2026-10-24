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
import java.util.UUID;

@Service
public class RecuperacaoSenhaService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacaoSenhaService.class);

    static final String ASSUNTO = "StockFlow - Redefinição de senha";
    static final String ASSUNTO_CONVITE = "StockFlow - Crie sua senha de acesso";
    static final String ASSUNTO_RESET = "StockFlow - Sua senha foi redefinida";
    static final String MENSAGEM_TOKEN_INVALIDO = "Link de redefinição inválido ou expirado.";

    static final Duration INTERVALO_MINIMO_ENTRE_PEDIDOS = Duration.ofMinutes(1);
    static final Duration VALIDADE_CONVITE = Duration.ofHours(24);

    static final String FRONT_PADRAO = "http://localhost:5173";

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacaoSenhaRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PublicadorDeMensagens publicador;
    private final String frontendUrl;
    private final Duration validade;
    private final SecureRandom secureRandom = new SecureRandom();

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

        String link = gerarLink(usuario, agora.plus(validade));

        publicador.publicar(
                RabbitMQConfig.FILA_EMAIL,
                new EmailMensagem(usuario.getEmail(), ASSUNTO, montarCorpo(usuario.getNome(), link))
        );
    }

    @Transactional
    public void convidar(Usuario usuario) {
        String link = gerarLink(usuario, Instant.now().plus(VALIDADE_CONVITE));

        publicador.publicar(
                RabbitMQConfig.FILA_EMAIL,
                new EmailMensagem(usuario.getEmail(), ASSUNTO_CONVITE,
                        montarCorpoComLinkLongo(usuario.getNome(), "Sua conta no StockFlow foi criada.", link))
        );
    }

    @Transactional
    public void resetar(Usuario usuario) {
        Instant agora = Instant.now();

        usuario.setSenhaHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuarioRepository.save(usuario);
        refreshTokenRepository.revogarTodosDoUsuario(usuario.getId(), agora);
        tokenRepository.invalidarPendentesDoUsuario(usuario.getId(), agora);

        String link = gerarLink(usuario, agora.plus(VALIDADE_CONVITE));

        publicador.publicar(
                RabbitMQConfig.FILA_EMAIL,
                new EmailMensagem(usuario.getEmail(), ASSUNTO_RESET,
                        montarCorpoComLinkLongo(usuario.getNome(), "Um administrador redefiniu a senha da sua conta no StockFlow.", link))
        );
    }

    private String gerarLink(Usuario usuario, Instant expiraEm) {
        String segredo = gerarSegredo();
        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
        token.setUsuario(usuario);
        token.setTokenHash(passwordEncoder.encode(segredo));
        token.setExpiraEm(expiraEm);
        TokenRecuperacaoSenha salvo = tokenRepository.save(token);

        return frontendUrl + "/redefinir-senha?token=" + salvo.getId() + "." + segredo;
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

    private String montarCorpoComLinkLongo(String nome, String motivo, String link) {
        return """
                Olá, %s!

                %s
                Para definir sua senha e acessar o sistema, use o link abaixo (válido por %d horas):

                %s

                Se o link expirar, use a opção "Esqueceu sua senha?" na tela de login.
                """.formatted(nome, motivo, VALIDADE_CONVITE.toHours(), link);
    }

    private String gerarSegredo() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
