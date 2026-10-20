package sound.pezao.backend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.RefreshToken;
import sound.pezao.backend.entities.TokenRecuperacaoSenha;
import sound.pezao.backend.entities.Usuario;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere as consultas de atualização em lote contra o banco de verdade (H2),
 * já que um erro de JPQL só aparece quando a consulta roda.
 */
@DataJpaTest
@DisplayName("Testes das consultas usadas na recuperação de senha")
class TokenRecuperacaoSenhaRepositoryTest {

    @Autowired
    private TokenRecuperacaoSenhaRepository tokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario ana;
    private Usuario bruno;

    @BeforeEach
    void setUp() {
        Cargo cargo = new Cargo();
        cargo.setNome("Vendedor");
        cargo.setDescricao("Cargo de teste");
        entityManager.persist(cargo);

        ana = usuario("ana@email.com", cargo);
        bruno = usuario("bruno@email.com", cargo);
    }

    private Usuario usuario(String email, Cargo cargo) {
        Usuario usuario = new Usuario();
        usuario.setNome(email);
        usuario.setEmail(email);
        usuario.setSenhaHash("hash");
        usuario.setAtivo(true);
        usuario.setCargo(cargo);
        return entityManager.persist(usuario);
    }

    private TokenRecuperacaoSenha token(Usuario usuario) {
        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
        token.setUsuario(usuario);
        token.setTokenHash("hash");
        token.setExpiraEm(Instant.now().plus(Duration.ofMinutes(30)));
        return entityManager.persist(token);
    }

    private RefreshToken refreshToken(Usuario usuario) {
        RefreshToken token = new RefreshToken();
        token.setUsuario(usuario);
        token.setTokenHash("hash");
        token.setExpiraEm(Instant.now().plus(Duration.ofDays(7)));
        return entityManager.persist(token);
    }

    @Test
    @DisplayName("Deve invalidar só os tokens pendentes do usuário informado")
    void deveInvalidarPendentesDoUsuario() {
        TokenRecuperacaoSenha daAna = token(ana);
        TokenRecuperacaoSenha doBruno = token(bruno);
        entityManager.flush();

        int afetados = tokenRepository.invalidarPendentesDoUsuario(ana.getId(), Instant.now());
        entityManager.clear();

        assertEquals(1, afetados);
        assertNotNull(tokenRepository.findById(daAna.getId()).orElseThrow().getUsadoEm());
        assertNull(tokenRepository.findById(doBruno.getId()).orElseThrow().getUsadoEm());
        assertTrue(tokenRepository.findByIdAndUsadoEmIsNull(daAna.getId()).isEmpty());
    }

    @Test
    @DisplayName("Deve identificar pedido feito há menos de um minuto")
    void deveIdentificarPedidoRecente() {
        token(ana);
        entityManager.flush();

        assertTrue(tokenRepository.existsByUsuario_IdAndCriadoEmAfter(ana.getId(), Instant.now().minus(Duration.ofMinutes(1))));
        assertFalse(tokenRepository.existsByUsuario_IdAndCriadoEmAfter(bruno.getId(), Instant.now().minus(Duration.ofMinutes(1))));
    }

    @Test
    @DisplayName("Deve encerrar todas as sessões do usuário e só as dele")
    void deveRevogarTodasAsSessoesDoUsuario() {
        RefreshToken sessao1 = refreshToken(ana);
        RefreshToken sessao2 = refreshToken(ana);
        RefreshToken doBruno = refreshToken(bruno);
        entityManager.flush();

        int afetados = refreshTokenRepository.revogarTodosDoUsuario(ana.getId(), Instant.now());
        entityManager.clear();

        assertEquals(2, afetados);
        assertFalse(refreshTokenRepository.findById(sessao1.getId()).orElseThrow().isAtivo());
        assertFalse(refreshTokenRepository.findById(sessao2.getId()).orElseThrow().isAtivo());
        assertTrue(refreshTokenRepository.findById(doBruno.getId()).orElseThrow().isAtivo());
    }
}
