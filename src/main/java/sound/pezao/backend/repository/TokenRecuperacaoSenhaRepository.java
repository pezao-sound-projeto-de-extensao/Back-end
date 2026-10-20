package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sound.pezao.backend.entities.TokenRecuperacaoSenha;

import java.time.Instant;
import java.util.Optional;

public interface TokenRecuperacaoSenhaRepository extends JpaRepository<TokenRecuperacaoSenha, Long> {

    Optional<TokenRecuperacaoSenha> findByIdAndUsadoEmIsNull(Long id);

    boolean existsByUsuario_IdAndCriadoEmAfter(Integer usuarioId, Instant limite);

    /**
     * Ao pedir um link novo, os anteriores deixam de valer:
     * só o e-mail mais recente funciona.
     */
    @Modifying
    @Query("""
            UPDATE TokenRecuperacaoSenha t
            SET t.usadoEm = :agora
            WHERE t.usuario.id = :usuarioId
            AND t.usadoEm IS NULL
            """)
    int invalidarPendentesDoUsuario(@Param("usuarioId") Integer usuarioId, @Param("agora") Instant agora);
}
