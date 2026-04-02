package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sound.pezao.backend.entities.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    boolean existsByEmailIgnoreCase(String email);
}
