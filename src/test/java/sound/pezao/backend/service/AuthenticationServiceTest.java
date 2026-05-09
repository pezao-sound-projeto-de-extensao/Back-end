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
import org.springframework.security.core.context.SecurityContextHolder;
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

        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Autenticação do usuário")
    class AuthenticateTest {

        @Test
        @DisplayName("Deve lançar LoginInvalidoException quando usuário não existe")
        void authenticateDeveLancarLoginInvalidoQuandoUsuarioNaoExiste() {
            AuthRequest request = new AuthRequest("teste@email.com", "123");

            when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.empty());

            assertThrows(LoginInvalidoException.class,
                    () -> authenticationService.authenticate(request));

            verify(authenticationManager, never()).authenticate(any());
            verify(usuarioRepository, never()).save(any());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Deve lançar PrimeiroAcessoException quando senha padrão")
        void authenticateDeveLancarPrimeiroAcessoQuandoSenhaPadrao() {
            AuthRequest request = new AuthRequest("teste@email.com", "123");

            when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(UsuarioService.senhaPadrao, usuario.getSenhaHash())).thenReturn(true);

            PrimeiroAcessoException ex = assertThrows(PrimeiroAcessoException.class,
                    () -> authenticationService.authenticate(request));

            assertEquals("Altere a senha padrão antes de efetuar o login", ex.getMessage());
            verify(authenticationManager, never()).authenticate(any());
            verify(usuarioRepository, never()).save(any());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Deve retornar AuthResponse quando credenciais válidas")
        void authenticateDeveRetornarAuthResponseQuandoCredenciaisValidas() {
            AuthRequest request = new AuthRequest("teste@email.com", "123");
            Authentication authentication = mock(Authentication.class);
            UserAuthenticated userAuthenticated = mock(UserAuthenticated.class);

            when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(UsuarioService.senhaPadrao, usuario.getSenhaHash())).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
            when(jwtService.generateToken(authentication)).thenReturn("jwt-token");
            when(authentication.getPrincipal()).thenReturn(userAuthenticated);
            when(userAuthenticated.getUsername()).thenReturn("teste@email.com");

            AuthResponse response = authenticationService.authenticate(request);

            assertNotNull(response);
            assertEquals("jwt-token", response.token());
            assertEquals("teste@email.com", response.username());
            assertNotNull(response.usuario());
            assertNotNull(usuario.getUltimoAcesso());

            verify(usuarioRepository).save(usuario);
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtService).generateToken(authentication);
        }
    }

    @Nested
    @DisplayName("Alteração de senha do usuário")
    class TrocarSenhaTest {

        @Test
        @DisplayName("Deve lançar LoginInvalidoException quando usuário não existe")
        void trocarSenhaDeveLancarLoginInvalidoQuandoUsuarioNaoExiste() {
            AuthTrocarSenhaRequest request =
                    new AuthTrocarSenhaRequest("teste@email.com", "atual", "nova");

            when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.empty());

            assertThrows(LoginInvalidoException.class,
                    () -> authenticationService.trocarSenha(request));

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar PrimeiroAcessoException quando senha já foi alterada")
        void trocarSenhaDeveLancarPrimeiroAcessoQuandoSenhaJaFoiAlterada() {
            AuthTrocarSenhaRequest request =
                    new AuthTrocarSenhaRequest("teste@email.com", "atual", "nova");

            when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(UsuarioService.senhaPadrao, usuario.getSenhaHash())).thenReturn(false);

            PrimeiroAcessoException ex = assertThrows(PrimeiroAcessoException.class,
                    () -> authenticationService.trocarSenha(request));

            assertEquals("A senha já foi alterada uma vez", ex.getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar LoginInvalidoException quando senha atual incorreta")
        void trocarSenhaDeveLancarLoginInvalidoQuandoSenhaAtualIncorreta() {
            AuthTrocarSenhaRequest request =
                    new AuthTrocarSenhaRequest("teste@email.com", "atual", "nova");

            when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(UsuarioService.senhaPadrao, usuario.getSenhaHash())).thenReturn(true);
            when(passwordEncoder.matches("atual", usuario.getSenhaHash())).thenReturn(false);

            assertThrows(LoginInvalidoException.class,
                    () -> authenticationService.trocarSenha(request));

            verify(passwordEncoder, never()).encode(any());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve salvar nova senha quando dados válidos")
        void trocarSenhaDeveSalvarNovaSenhaQuandoDadosValidos() {
            AuthTrocarSenhaRequest request =
                    new AuthTrocarSenhaRequest("teste@email.com", "atual", "nova");
            String novaHash = "hash-nova";

            when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(UsuarioService.senhaPadrao, usuario.getSenhaHash())).thenReturn(true);
            when(passwordEncoder.matches("atual", usuario.getSenhaHash())).thenReturn(true);
            when(passwordEncoder.encode("nova")).thenReturn(novaHash);

            authenticationService.trocarSenha(request);

            assertEquals(novaHash, usuario.getSenhaHash());
            verify(passwordEncoder).encode("nova");
            verify(usuarioRepository).save(usuario);
        }
    }

    @Nested
    @DisplayName("Voltar a senha do usuário para a senha padrão")
    class ResetarSenhaTest {

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando usuário não existe")
        void resetarSenhaDeveLancarEntityNotFoundQuandoUsuarioNaoExiste() {
            when(usuarioRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> authenticationService.resetarSenha(1));

            verify(passwordEncoder, never()).encode(any());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve salvar senha padrão quando usuário existe")
        void resetarSenhaDeveSalvarSenhaPadraoQuandoUsuarioExiste() {
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.encode(UsuarioService.senhaPadrao)).thenReturn("hash-padrao");

            authenticationService.resetarSenha(1);

            assertEquals("hash-padrao", usuario.getSenhaHash());
            verify(passwordEncoder).encode(UsuarioService.senhaPadrao);
            verify(usuarioRepository).save(usuario);
        }
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
                PasswordEncoder passwordEncoder
        ) {
            return new AuthenticationService(
                    jwtService,
                    authenticationManager,
                    usuarioRepository,
                    passwordEncoder
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

    @Resource
    private AuthenticationService authenticationService;

    @Nested
    @DisplayName("resetarSenha")
    class ResetarSenhaTest {

        @Test
        @WithMockUser(authorities = "GERENCIAR_USUARIOS")
        @DisplayName("Deve permitir quando tem permissão")
        void devePermitirQuandoTemPermissao() {
            Usuario usuario = new Usuario();

            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.encode(UsuarioService.senhaPadrao)).thenReturn("hash-padrao");

            assertDoesNotThrow(() -> authenticationService.resetarSenha(1));

            verify(usuarioRepository).findById(1);
            verify(passwordEncoder).encode(UsuarioService.senhaPadrao);
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @WithMockUser(authorities = "OUTRA_PERMISSAO")
        @DisplayName("Deve negar quando não tem permissão")
        void deveNegarQuandoNaoTemPermissao() {
            assertThrows(AccessDeniedException.class,
                    () -> authenticationService.resetarSenha(1));

            verify(usuarioRepository, never()).findById(anyInt());
            verify(passwordEncoder, never()).encode(any());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("Deve negar quando anônimo")
        void deveNegarQuandoAnonimo() {
            assertThrows(AccessDeniedException.class,
                    () -> authenticationService.resetarSenha(1));

            verify(usuarioRepository, never()).findById(anyInt());
            verify(passwordEncoder, never()).encode(any());
            verify(usuarioRepository, never()).save(any());
        }
    }
}