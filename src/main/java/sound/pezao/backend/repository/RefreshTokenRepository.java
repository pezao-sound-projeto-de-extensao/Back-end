package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sound.pezao.backend.entities.RefreshToken;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByIdAndRevogadoEmIsNull(Long id);

    /**
     * Encerra todas as sessões do usuário. Usado quando a senha é redefinida:
     * quem estava logado com a senha antiga precisa entrar de novo.
     */
    @Modifying
    @Query("""
            UPDATE RefreshToken r
            SET r.revogadoEm = :agora
            WHERE r.usuario.id = :usuarioId
            AND r.revogadoEm IS NULL
            """)
    int revogarTodosDoUsuario(@Param("usuarioId") Integer usuarioId, @Param("agora") Instant agora);
}
