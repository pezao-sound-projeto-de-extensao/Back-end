package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sound.pezao.backend.entities.Permissao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissaoRepository extends JpaRepository<Permissao, Integer> {
  Optional<Permissao> findByNome(String nome);

  List<Permissao> findByNomeIn(Set<String> nomes);
}
