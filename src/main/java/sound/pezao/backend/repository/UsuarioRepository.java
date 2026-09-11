package sound.pezao.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sound.pezao.backend.entities.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Listagem da tela de usuários: busca por nome ou e-mail e filtro por cargo.
     * O EntityGraph carrega o cargo junto porque o mapper o acessa para montar a
     * resposta — sem ele a listagem faria uma consulta por linha.
     */
    @EntityGraph(attributePaths = "cargo")
    @Query("""
        SELECT u FROM Usuario u
        WHERE (:search IS NULL
            OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:cargoId IS NULL OR u.cargo.id = :cargoId)
    """)
    Page<Usuario> findAllFiltered(
            @Param("search") String search,
            @Param("cargoId") Integer cargoId,
            Pageable pageable
    );

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCargo_Id(Integer cargoId);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, int id);

    @Query("select u from Usuario u join fetch u.cargo c join fetch c.permissoes where u.email = :email")
    Optional<Usuario> findByEmailCompleto(String email);

    Optional<Usuario> findByEmail(String email);
}
