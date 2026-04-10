package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sound.pezao.backend.entities.Permissao;

public interface PermissaoRepository extends JpaRepository<Permissao, Integer> {
}
