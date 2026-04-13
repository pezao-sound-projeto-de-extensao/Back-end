package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sound.pezao.backend.entities.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, int id);

    @Query("select u from Usuario u join fetch u.cargo c join fetch c.permissoes where u.email = :email")
    Optional<Usuario> findByEmailCompleto(String username);

    Optional<Usuario> findByEmail(String email);
}
