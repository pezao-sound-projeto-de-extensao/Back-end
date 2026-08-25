package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sound.pezao.backend.entities.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByIdAndRevogadoEmIsNull(Long id);
}
