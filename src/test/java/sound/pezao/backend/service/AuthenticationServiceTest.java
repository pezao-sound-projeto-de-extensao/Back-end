package sound.pezao.backend.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import sound.pezao.backend.dto.authDTO.AuthRequest;
import sound.pezao.backend.dto.authDTO.AuthResponse;
import sound.pezao.backend.dto.authDTO.AuthTrocarSenhaRequest;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Permissao;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.exception.LoginInvalidoException;
import sound.pezao.backend.exception.PrimeiroAcessoException;
import sound.pezao.backend.repository.UsuarioRepository;
import sound.pezao.backend.security.JwtService;
import sound.pezao.backend.security.UserAuthenticated;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de negócio da AuthenticationService")
class AuthenticationServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        Permissao permissao = new Permissao();
        permissao.setId(1);
        permissao.setNome("GERENCIAR_USUARIOS");
        permissao.setDescricao("Permissão de teste");

        Cargo cargo = new Cargo();
        cargo.setId(1);
        cargo.setNome("Administrador");
        cargo.setDescricao("Cargo de teste");
        cargo.getPermissoes().add(permissao);

        usuario = new Usuario();
        usuario.setId(1);
        usuario.setEmail("teste@email.com");
        usuario.setSenhaHash("hash");
        usuario.setCargo(cargo);
        usuario.setAtivo(true);
    }

    @Nested
    @DisplayName("Autenticação do usuário")
    class AuthenticateTest {

        @Test
        @DisplayName("Deve lançar LoginInvalidoException quando usuário não existe")
        void deveLancarLoginInvalidoQuandoUsuarioNaoExiste() {
            AuthRequest request =
                    new AuthRequest("teste@email.com", "123");

            when(usuarioRepository.findByEmail("teste@email.com"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    LoginInvalidoException.class,
                    () -> authenticationService.authenticate(request)
            );

            verify(authenticationManager, never())
                    .authenticate(any());
            verify(passwordEncoder, never())
                    .matches(any(), any());
            verify(usuarioRepository, never())
                    .save(any());
            verify(jwtService, never())
                    .generateToken(any());
            verify(refreshTokenService, never())
                    .criar(any());
        }

        @Test
        @DisplayName("Deve lançar PrimeiroAcessoException quando senha padrão")
        void deveLancarPrimeiroAcessoQuandoSenhaPadrao() {
            AuthRequest request =
                    new AuthRequest("teste@email.com", "123");

            when(usuarioRepository.findByEmail("teste@email.com"))
                    .thenReturn(Optional.of(usuario));

            when(passwordEncoder.matches(
                    UsuarioService.senhaPadrao,
                    usuario.getSenhaHash()
            )).thenReturn(true);

            PrimeiroAcessoException exception =
                    assertThrows(
                            PrimeiroAcessoException.class,
                            () -> authenticationService.authenticate(request)
                    );

            assertEquals(
                    "Altere a senha padrão antes de efetuar o login",
                    exception.getMessage()
            );

            verify(passwordEncoder).matches(
                    UsuarioService.senhaPadrao,
                    usuario.getSenhaHash()
            );
            verify(authenticationManager, never())
                    .authenticate(any());
            verify(usuarioRepository, never())
                    .save(any());
            verify(jwtService, never())
                    .generateToken(any());
            verify(refreshTokenService, never())
                    .criar(any());
        }

        @Test
        @DisplayName("Deve retornar AuthResponse quando credenciais válidas")
        void deveRetornarAuthResponseQuandoCredenciaisValidas() {
            AuthRequest request =
                    new AuthRequest("teste@email.com", "123");

            Authentication authentication = mockAuthentication();
            UserAuthenticated userDetails = mockUserDetails();

            when(usuarioRepository.findByEmail("teste@email.com"))
                    .thenReturn(Optional.of(usuario));

            when(passwordEncoder.matches(
                    UsuarioService.senhaPadrao,
                    usuario.getSenhaHash()
            )).thenReturn(false);

            when(authenticationManager.authenticate(
                    any(UsernamePasswordAuthenticationToken.class)
            )).thenReturn(authentication);

            when(jwtService.generateToken(authentication))
                    .thenReturn("access-token");

            when(refreshTokenService.criar(usuario))
                    .thenReturn("1.refresh-token");

            when(authentication.getPrincipal())
                    .thenReturn(userDetails);

            when(userDetails.getUsername())
                    .thenReturn("teste@email.com");

            AuthResponse response =
                    authenticationService.authenticate(request);

            assertNotNull(response);
            assertEquals("access-token", response.accessToken());
            assertEquals("1.refresh-token", response.refreshToken());
            assertEquals("teste@email.com", response.username());
            assertNotNull(response.usuario());
            assertNotNull(usuario.getUltimoAcesso());

            verify(passwordEncoder).matches(
                    UsuarioService.senhaPadrao,
                    usuario.getSenhaHash()
            );
            verify(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(usuarioRepository).save(usuario);
            verify(jwtService).generateToken(authentication);
            verify(refreshTokenService).criar(usuario);
        }
    }

    @Nested
    @DisplayName("Alteração de senha do usuário")
    class TrocarSenhaTest {

        @Test
        @DisplayName("Deve lançar LoginInvalidoException quando usuário não existe")
        void deveLancarLoginInvalidoQuandoUsuarioNaoExiste() {
            AuthTrocarSenhaRequest request =
                    new AuthTrocarSenhaRequest(
                            "teste@email.com",
                            "atual",
                            "nova"
                    );

            when(usuarioRepository.findByEmail("teste@email.com"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    LoginInvalidoException.class,
                    () -> authenticationService.trocarSenha(request)
            );

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar PrimeiroAcessoException quando senha já foi alterada")
        void deveLancarPrimeiroAcessoQuandoSenhaJaFoiAlterada() {
            AuthTrocarSenhaRequest request =
                    new AuthTrocarSenhaRequest(
                            "teste@email.com",
                            "atual",
                            "nova"
                    );

            when(usuarioRepository.findByEmail("teste@email.com"))
                    .thenReturn(Optional.of(usuario));

            when(passwordEncoder.matches(
                    UsuarioService.senhaPadrao,
                    usuario.getSenhaHash()
            )).thenReturn(false);

            PrimeiroAcessoException exception =
                    assertThrows(
                            PrimeiroAcessoException.class,
                            () -> authenticationService.trocarSenha(request)
                    );

            assertEquals(
                    "A senha já foi alterada uma vez",
                    exception.getMessage()
            );

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar LoginInvalidoException quando senha atual incorreta")
        void deveLancarLoginInvalidoQuandoSenhaAtualIncorreta() {
            AuthTrocarSenhaRequest request =
                    new AuthTrocarSenhaRequest(
                            "teste@email.com",
                            "atual",
                            "nova"
                    );

            when(usuarioRepository.findByEmail("teste@email.com"))
                    .thenReturn(Optional.of(usuario));

            when(passwordEncoder.matches(
                    UsuarioService.senhaPadrao,
                    usuario.getSenhaHash()
            )).thenReturn(true);

            when(passwordEncoder.matches(
                    "atual",
                    usuario.getSenhaHash()
            )).thenReturn(false);

            assertThrows(
                    LoginInvalidoException.class,
                    () -> authenticationService.trocarSenha(request)
            );

            verify(passwordEncoder, never()).encode(any());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve salvar nova senha quando dados válidos")
        void deveSalvarNovaSenhaQuandoDadosValidos() {
            AuthTrocarSenhaRequest request =
                    new AuthTrocarSenhaRequest(
                            "teste@email.com",
                            "atual",
                            "nova"
                    );

            when(usuarioRepository.findByEmail("teste@email.com"))
                    .thenReturn(Optional.of(usuario));

            when(passwordEncoder.matches(
                    UsuarioService.senhaPadrao,
                    usuario.getSenhaHash()
            )).thenReturn(true);

            when(passwordEncoder.matches(
                    "atual",
                    usuario.getSenhaHash()
            )).thenReturn(true);

            when(passwordEncoder.encode("nova"))
                    .thenReturn("hash-nova");

            authenticationService.trocarSenha(request);

            assertEquals("hash-nova", usuario.getSenhaHash());
            verify(passwordEncoder).encode("nova");
            verify(usuarioRepository).save(usuario);
        }
    }

    @Nested
    @DisplayName("Resetar senha do usuário")
    class ResetarSenhaTest {

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando usuário não existe")
        void deveLancarEntityNotFoundQuandoUsuarioNaoExiste() {
            when(usuarioRepository.findById(1))
                    .thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> authenticationService.resetarSenha(1)
            );

            verify(passwordEncoder, never()).encode(any());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve salvar senha padrão quando usuário existe")
        void deveSalvarSenhaPadraoQuandoUsuarioExiste() {
            when(usuarioRepository.findById(1))
                    .thenReturn(Optional.of(usuario));

            when(passwordEncoder.encode(UsuarioService.senhaPadrao))
                    .thenReturn("hash-padrao");

            authenticationService.resetarSenha(1);

            assertEquals("hash-padrao", usuario.getSenhaHash());
            verify(passwordEncoder)
                    .encode(UsuarioService.senhaPadrao);
            verify(usuarioRepository).save(usuario);
        }
    }

    @Nested
    @DisplayName("Refresh token")
    class RefreshTokenTest {

        @Test
        @DisplayName("Deve renovar os tokens")
        void deveRenovarOsTokens() {
            String refreshToken = "1.refresh-token";

            when(refreshTokenService.validar(refreshToken))
                    .thenReturn(usuario);

            when(jwtService.generateToken(any(Authentication.class)))
                    .thenReturn("novo-access-token");

            when(refreshTokenService.criar(usuario))
                    .thenReturn("2.novo-refresh-token");

            AuthResponse response =
                    authenticationService.refreshToken(refreshToken);

            assertNotNull(response);
            assertEquals("novo-access-token", response.accessToken());
            assertEquals("2.novo-refresh-token", response.refreshToken());
            assertEquals("teste@email.com", response.username());

            verify(refreshTokenService).validar(refreshToken);
            verify(jwtService).generateToken(any(Authentication.class));
            verify(refreshTokenService).criar(usuario);
        }

        @Test
        @DisplayName("Deve propagar erro quando refresh token inválido")
        void devePropagarErroQuandoRefreshTokenInvalido() {
            String refreshToken = "token-invalido";

            when(refreshTokenService.validar(refreshToken))
                    .thenThrow(new IllegalArgumentException(
                            "Refresh token inválido"
                    ));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> authenticationService.refreshToken(refreshToken)
            );

            verify(jwtService, never())
                    .generateToken(any());
            verify(refreshTokenService, never())
                    .criar(any());
        }
    }

    @Nested
    @DisplayName("Logout")
    class LogoutTest {

        @Test
        @DisplayName("Deve revogar o refresh token")
        void deveRevogarRefreshToken() {
            String refreshToken = "1.refresh-token";

            authenticationService.logout(refreshToken);

            verify(refreshTokenService).revogar(refreshToken);
        }
    }

    private Authentication mockAuthentication() {
        return mock(Authentication.class);
    }

    private UserAuthenticated mockUserDetails() {
        return mock(UserAuthenticated.class);
    }
}

@SpringJUnitConfig(AuthenticationServiceSecurityTest.TestConfig.class)
@DisplayName("Testes de segurança da AuthenticationService")
class AuthenticationServiceSecurityTest {

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        AuthenticationService authenticationService(
                JwtService jwtService,
                AuthenticationManager authenticationManager,
                UsuarioRepository usuarioRepository,
                PasswordEncoder passwordEncoder,
                RefreshTokenService refreshTokenService
        ) {
            return new AuthenticationService(
                    jwtService,
                    authenticationManager,
                    usuarioRepository,
                    passwordEncoder,
                    refreshTokenService
            );
        }
    }

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @Resource
    private AuthenticationService authenticationService;

    @Test
    @WithMockUser(authorities = "GERENCIAR_USUARIOS")
    @DisplayName("Deve permitir resetar senha quando tem permissão")
    void devePermitirResetarSenhaQuandoTemPermissao() {
        Usuario usuario = new Usuario();

        when(usuarioRepository.findById(1))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.encode(UsuarioService.senhaPadrao))
                .thenReturn("hash-padrao");

        assertDoesNotThrow(
                () -> authenticationService.resetarSenha(1)
        );

        verify(usuarioRepository).findById(1);
        verify(passwordEncoder)
                .encode(UsuarioService.senhaPadrao);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @WithMockUser(authorities = "OUTRA_PERMISSAO")
    @DisplayName("Deve negar resetar senha sem permissão")
    void deveNegarResetarSenhaSemPermissao() {
        assertThrows(
                AccessDeniedException.class,
                () -> authenticationService.resetarSenha(1)
        );

        verify(usuarioRepository, never()).findById(any());
        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Deve negar resetar senha para usuário anônimo")
    void deveNegarResetarSenhaParaUsuarioAnonimo() {
        assertThrows(
                AccessDeniedException.class,
                () -> authenticationService.resetarSenha(1)
        );

        verify(usuarioRepository, never()).findById(any());
        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).save(any());
    }
}