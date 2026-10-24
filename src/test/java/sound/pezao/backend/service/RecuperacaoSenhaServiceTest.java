package sound.pezao.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import sound.pezao.backend.config.RabbitMQConfig;
import sound.pezao.backend.entities.TokenRecuperacaoSenha;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.mensageria.PublicadorDeMensagens;
import sound.pezao.backend.mensageria.email.EmailMensagem;
import sound.pezao.backend.repository.RefreshTokenRepository;
import sound.pezao.backend.repository.TokenRecuperacaoSenhaRepository;
import sound.pezao.backend.repository.UsuarioRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para RecuperacaoSenhaService")
class RecuperacaoSenhaServiceTest {

    private static final String CORS_PRODUCAO = "stockflow-alb-123.us-east-1.elb.amazonaws.com";
    private static final String FRONTEND = "http://" + CORS_PRODUCAO;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenRecuperacaoSenhaRepository tokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PublicadorDeMensagens publicador;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private RecuperacaoSenhaService service;

    @BeforeEach
    void setUp() {
        service = new RecuperacaoSenhaService(
                usuarioRepository, tokenRepository, refreshTokenRepository,
                passwordEncoder, publicador, CORS_PRODUCAO, Duration.ofMinutes(30)
        );
    }

    private Usuario usuario(boolean ativo) {
        return Usuario.builder()
                .id(1)
                .nome("Ana")
                .email("ana@email.com")
                .senhaHash(passwordEncoder.encode("senhaAntiga"))
                .ativo(ativo)
                .build();
    }

    private TokenRecuperacaoSenha token(Usuario usuario, String segredo, Instant expiraEm) {
        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
        token.setId(7L);
        token.setUsuario(usuario);
        token.setTokenHash(passwordEncoder.encode(segredo));
        token.setExpiraEm(expiraEm);
        return token;
    }

    private EmailMensagem solicitarEObterEmail(Usuario usuario) {
        when(usuarioRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));
        when(tokenRepository.save(any(TokenRecuperacaoSenha.class))).thenAnswer(invocation -> {
            TokenRecuperacaoSenha salvo = invocation.getArgument(0);
            salvo.setId(7L);
            return salvo;
        });

        service.solicitar("ana@email.com");

        ArgumentCaptor<EmailMensagem> captor = ArgumentCaptor.forClass(EmailMensagem.class);
        verify(publicador).publicar(eq(RabbitMQConfig.FILA_EMAIL), captor.capture());
        return captor.getValue();
    }

    @ParameterizedTest(name = "CORS \"{0}\" vira \"{1}\"")
    @CsvSource(delimiter = '|', value = {
            "stockflow-alb.elb.amazonaws.com             | http://stockflow-alb.elb.amazonaws.com",
            "http://localhost:8080                       | http://localhost:8080",
            "https://stockflow.com.br/                   | https://stockflow.com.br",
            "http://localhost:8080,http://localhost:5173 | http://localhost:8080",
            "*                                           | http://localhost:5173",
            "''                                          | http://localhost:5173"
    })
    @DisplayName("Deve transformar o valor do CORS no endereço do link")
    void deveTransformarCorsEmEnderecoDoFront(String cors, String esperado) {
        assertEquals(esperado, RecuperacaoSenhaService.enderecoDoFront(cors));
    }

    @Test
    @DisplayName("Deve usar o endereço padrão quando o CORS não estiver definido")
    void deveUsarPadraoSemCors() {
        assertEquals(RecuperacaoSenhaService.FRONT_PADRAO, RecuperacaoSenhaService.enderecoDoFront(null));
    }

    @Test
    @DisplayName("Deve gerar o token e publicar o e-mail com o link na fila")
    void deveGerarTokenEPublicarEmail() {
        Usuario usuario = usuario(true);

        EmailMensagem email = solicitarEObterEmail(usuario);

        assertEquals("ana@email.com", email.destinatario());
        assertEquals(RecuperacaoSenhaService.ASSUNTO, email.assunto());
        assertTrue(email.corpo().contains(FRONTEND + "/redefinir-senha?token=7."));
        assertTrue(email.corpo().contains("30 minutos"));
        verify(tokenRepository).invalidarPendentesDoUsuario(eq(1), any(Instant.class));
    }

    @Test
    @DisplayName("O token deve expirar em 30 minutos e o banco guardar só o hash")
    void deveSalvarApenasOHashComValidade() {
        Usuario usuario = usuario(true);

        EmailMensagem email = solicitarEObterEmail(usuario);

        ArgumentCaptor<TokenRecuperacaoSenha> captor = ArgumentCaptor.forClass(TokenRecuperacaoSenha.class);
        verify(tokenRepository).save(captor.capture());
        TokenRecuperacaoSenha salvo = captor.getValue();
        String segredo = email.corpo().split("token=7\\.")[1].lines().findFirst().orElseThrow();

        assertTrue(passwordEncoder.matches(segredo, salvo.getTokenHash()));
        assertTrue(salvo.getExpiraEm().isAfter(Instant.now().plus(Duration.ofMinutes(29))));
        assertTrue(salvo.getExpiraEm().isBefore(Instant.now().plus(Duration.ofMinutes(31))));
    }

    @Test
    @DisplayName("Deve enviar o convite com link válido por 24 horas")
    void deveEnviarConvite() {
        Usuario usuario = usuario(true);
        when(tokenRepository.save(any(TokenRecuperacaoSenha.class))).thenAnswer(invocation -> {
            TokenRecuperacaoSenha salvo = invocation.getArgument(0);
            salvo.setId(7L);
            return salvo;
        });

        service.convidar(usuario);

        ArgumentCaptor<EmailMensagem> email = ArgumentCaptor.forClass(EmailMensagem.class);
        verify(publicador).publicar(eq(RabbitMQConfig.FILA_EMAIL), email.capture());
        assertEquals("ana@email.com", email.getValue().destinatario());
        assertEquals(RecuperacaoSenhaService.ASSUNTO_CONVITE, email.getValue().assunto());
        assertTrue(email.getValue().corpo().contains(FRONTEND + "/redefinir-senha?token=7."));
        assertTrue(email.getValue().corpo().contains("24 horas"));

        ArgumentCaptor<TokenRecuperacaoSenha> token = ArgumentCaptor.forClass(TokenRecuperacaoSenha.class);
        verify(tokenRepository).save(token.capture());
        assertTrue(token.getValue().getExpiraEm().isAfter(Instant.now().plus(Duration.ofHours(23))));
        verify(tokenRepository, never()).invalidarPendentesDoUsuario(anyInt(), any());
    }

    @Test
    @DisplayName("O reset do admin deve invalidar a senha e as sessões e enviar o link")
    void deveResetarEnviarLink() {
        Usuario usuario = usuario(true);
        String hashAntigo = usuario.getSenhaHash();
        when(tokenRepository.save(any(TokenRecuperacaoSenha.class))).thenAnswer(invocation -> {
            TokenRecuperacaoSenha salvo = invocation.getArgument(0);
            salvo.setId(7L);
            return salvo;
        });

        service.resetar(usuario);

        assertNotEquals(hashAntigo, usuario.getSenhaHash());
        assertFalse(passwordEncoder.matches("senhaAntiga", usuario.getSenhaHash()));
        verify(usuarioRepository).save(usuario);
        verify(refreshTokenRepository).revogarTodosDoUsuario(eq(1), any(Instant.class));
        verify(tokenRepository).invalidarPendentesDoUsuario(eq(1), any(Instant.class));

        ArgumentCaptor<EmailMensagem> email = ArgumentCaptor.forClass(EmailMensagem.class);
        verify(publicador).publicar(eq(RabbitMQConfig.FILA_EMAIL), email.capture());
        assertEquals(RecuperacaoSenhaService.ASSUNTO_RESET, email.getValue().assunto());
        assertTrue(email.getValue().corpo().contains(FRONTEND + "/redefinir-senha?token=7."));
    }

    @Test
    @DisplayName("Não deve revelar nem enviar nada para e-mail sem cadastro")
    void naoDeveEnviarParaEmailInexistente() {
        when(usuarioRepository.findByEmailIgnoreCase("ninguem@email.com")).thenReturn(Optional.empty());

        service.solicitar("ninguem@email.com");

        verifyNoInteractions(tokenRepository, publicador);
    }

    @Test
    @DisplayName("Não deve enviar link para usuário inativo")
    void naoDeveEnviarParaUsuarioInativo() {
        when(usuarioRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario(false)));

        service.solicitar("ana@email.com");

        verifyNoInteractions(tokenRepository, publicador);
    }

    @Test
    @DisplayName("Não deve enviar outro e-mail se houve um pedido há menos de um minuto")
    void naoDeveEnviarPedidoRepetido() {
        when(usuarioRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario(true)));
        when(tokenRepository.existsByUsuario_IdAndCriadoEmAfter(eq(1), any(Instant.class))).thenReturn(true);

        service.solicitar("ana@email.com");

        verify(tokenRepository, never()).save(any());
        verifyNoInteractions(publicador);
    }

    @Test
    @DisplayName("Deve trocar a senha, consumir o token e encerrar as sessões abertas")
    void deveRedefinirComTokenValido() {
        Usuario usuario = usuario(true);
        TokenRecuperacaoSenha token = token(usuario, "segredo", Instant.now().plus(Duration.ofMinutes(10)));
        when(tokenRepository.findByIdAndUsadoEmIsNull(7L)).thenReturn(Optional.of(token));

        service.redefinir("7.segredo", "novaSenha");

        assertTrue(passwordEncoder.matches("novaSenha", usuario.getSenhaHash()));
        assertNotNull(token.getUsadoEm());
        verify(usuarioRepository).save(usuario);
        verify(refreshTokenRepository).revogarTodosDoUsuario(eq(1), any(Instant.class));
    }

    @Test
    @DisplayName("O link do e-mail deve funcionar na redefinição")
    void linkDoEmailDeveFuncionarNaRedefinicao() {
        Usuario usuario = usuario(true);
        EmailMensagem email = solicitarEObterEmail(usuario);

        ArgumentCaptor<TokenRecuperacaoSenha> captor = ArgumentCaptor.forClass(TokenRecuperacaoSenha.class);
        verify(tokenRepository).save(captor.capture());
        when(tokenRepository.findByIdAndUsadoEmIsNull(7L)).thenReturn(Optional.of(captor.getValue()));
        String tokenDoLink = email.corpo().split("token=")[1].lines().findFirst().orElseThrow();

        service.redefinir(tokenDoLink, "novaSenha");

        assertTrue(passwordEncoder.matches("novaSenha", usuario.getSenhaHash()));
    }

    @Test
    @DisplayName("Deve recusar segredo errado")
    void deveRecusarSegredoErrado() {
        Usuario usuario = usuario(true);
        when(tokenRepository.findByIdAndUsadoEmIsNull(7L))
                .thenReturn(Optional.of(token(usuario, "segredo", Instant.now().plus(Duration.ofMinutes(10)))));

        assertThrows(IllegalArgumentException.class, () -> service.redefinir("7.outro", "novaSenha"));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve recusar token expirado")
    void deveRecusarTokenExpirado() {
        Usuario usuario = usuario(true);
        when(tokenRepository.findByIdAndUsadoEmIsNull(7L))
                .thenReturn(Optional.of(token(usuario, "segredo", Instant.now().minus(Duration.ofMinutes(1)))));

        assertThrows(IllegalArgumentException.class, () -> service.redefinir("7.segredo", "novaSenha"));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve recusar token já usado ou inexistente")
    void deveRecusarTokenJaUsado() {
        when(tokenRepository.findByIdAndUsadoEmIsNull(7L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.redefinir("7.segredo", "novaSenha"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "semponto", "abc.segredo"})
    @DisplayName("Deve recusar token em formato inválido sem consultar o banco")
    void deveRecusarTokenMalformado(String token) {
        assertThrows(IllegalArgumentException.class, () -> service.redefinir(token, "novaSenha"));

        verify(tokenRepository, never()).findByIdAndUsadoEmIsNull(anyLong());
        verify(refreshTokenRepository, never()).revogarTodosDoUsuario(anyInt(), any());
    }
}
