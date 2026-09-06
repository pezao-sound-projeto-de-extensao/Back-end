package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sound.pezao.backend.entities.RefreshToken;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para RefreshTokenService")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RefreshTokenService service;

    @Test
    @DisplayName("Deve criar refresh token com sucesso")
    void deveCriarRefreshTokenComSucesso() {
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setNome("Teste");
        usuario.setEmail("teste@example.com");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUsuario(usuario);
        refreshToken.setTokenHash("hash-do-token");
        refreshToken.setExpiraEm(Instant.now().plusSeconds(604800));

        when(passwordEncoder.encode(anyString())).thenReturn("hash-do-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        String token = service.criar(usuario);

        assertNotNull(token);
        assertTrue(token.startsWith("1."));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve codificar o segredo ao criar refresh token")
    void deveCodificarSegredoAoCriar() {
        Usuario usuario = new Usuario();
        usuario.setId(1);

        RefreshToken refreshTokenSalvo = new RefreshToken();
        refreshTokenSalvo.setId(1L);
        refreshTokenSalvo.setUsuario(usuario);
        refreshTokenSalvo.setTokenHash("hash-codificado");
        refreshTokenSalvo.setExpiraEm(Instant.now().plusSeconds(604800));

        when(passwordEncoder.encode(anyString())).thenReturn("hash-codificado");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshTokenSalvo);

        service.criar(usuario);

        verify(passwordEncoder).encode(anyString());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve validar refresh token com sucesso")
    void deveValidarRefreshTokenComSucesso() {
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setNome("Teste");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUsuario(usuario);
        refreshToken.setTokenHash("hash-do-token");
        refreshToken.setExpiraEm(Instant.now().plusSeconds(604800));

        when(refreshTokenRepository.findByIdAndRevogadoEmIsNull(1L))
                .thenReturn(Optional.of(refreshToken));
        when(passwordEncoder.matches("segredo", "hash-do-token")).thenReturn(true);

        Usuario resultado = service.validar("1.segredo");

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(refreshTokenRepository).findByIdAndRevogadoEmIsNull(1L);
    }

    @Test
    @DisplayName("Deve revogar refresh token após validação")
    void deveRevogarRefreshTokenAposValidacao() {
        Usuario usuario = new Usuario();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUsuario(usuario);
        refreshToken.setTokenHash("hash-do-token");
        refreshToken.setExpiraEm(Instant.now().plusSeconds(604800));

        when(refreshTokenRepository.findByIdAndRevogadoEmIsNull(1L))
                .thenReturn(Optional.of(refreshToken));
        when(passwordEncoder.matches("segredo", "hash-do-token")).thenReturn(true);

        service.validar("1.segredo");

        assertNotNull(refreshToken.getRevogadoEm());
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar token com formato inválido")
    void deveLancarExcecaoAoValidarTokenComFormatoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> service.validar("token-sem-ponto"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar token com ID inválido")
    void deveLancarExcecaoAoValidarTokenComIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> service.validar("abc.segredo"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar token inexistente")
    void deveLancarExcecaoAoValidarTokenInexistente() {
        when(refreshTokenRepository.findByIdAndRevogadoEmIsNull(1L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.validar("1.segredo"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar token com segredo incorreto")
    void deveLancarExcecaoAoValidarTokenComSegredoIncorreto() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setTokenHash("hash-do-token");

        when(refreshTokenRepository.findByIdAndRevogadoEmIsNull(1L))
                .thenReturn(Optional.of(refreshToken));
        when(passwordEncoder.matches("segredo-errado", "hash-do-token")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.validar("1.segredo-errado"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar token revogado")
    void deveLancarExcecaoAoValidarTokenRevogado() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setTokenHash("hash-do-token");
        refreshToken.setRevogadoEm(Instant.now());

        when(refreshTokenRepository.findByIdAndRevogadoEmIsNull(1L))
                .thenReturn(Optional.of(refreshToken));

        assertThrows(IllegalArgumentException.class, () -> service.validar("1.segredo"));
    }

    @Test
    @DisplayName("Deve revogar refresh token com sucesso")
    void deveRevogarRefreshTokenComSucesso() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setTokenHash("hash-do-token");

        when(refreshTokenRepository.findByIdAndRevogadoEmIsNull(1L))
                .thenReturn(Optional.of(refreshToken));
        when(passwordEncoder.matches("segredo", "hash-do-token")).thenReturn(true);

        service.revogar("1.segredo");

        assertNotNull(refreshToken.getRevogadoEm());
    }

    @Test
    @DisplayName("Não deve falhar ao revogar token com formato inválido")
    void naoDeveFalharAoRevogarTokenComFormatoInvalido() {
        service.revogar("token-sem-ponto");
    }

    @Test
    @DisplayName("Não deve falhar ao revogar token com ID inválido")
    void naoDeveFalharAoRevogarTokenComIdInvalido() {
        service.revogar("abc.segredo");
    }

    @Test
    @DisplayName("Não deve revogar token inexistente")
    void naoDeveRevogarTokenInexistente() {
        when(refreshTokenRepository.findByIdAndRevogadoEmIsNull(1L))
                .thenReturn(Optional.empty());

        service.revogar("1.segredo");
    }

    @Test
    @DisplayName("Não deve revogar token com segredo incorreto")
    void naoDeveRevogarTokenComSegredoIncorreto() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setTokenHash("hash-do-token");

        when(refreshTokenRepository.findByIdAndRevogadoEmIsNull(1L))
                .thenReturn(Optional.of(refreshToken));
        when(passwordEncoder.matches("segredo-errado", "hash-do-token")).thenReturn(false);

        service.revogar("1.segredo-errado");

        assertNull(refreshToken.getRevogadoEm());
    }
}