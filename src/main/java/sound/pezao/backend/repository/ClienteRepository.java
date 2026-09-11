package sound.pezao.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sound.pezao.backend.entities.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    @Query("""
        SELECT c FROM Cliente c
        WHERE (:search IS NULL
            OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :search, '%'))
            OR c.telefone LIKE CONCAT('%', :search, '%'))
        ORDER BY c.nome ASC
    """)
    Page<Cliente> findAllFiltered(@Param("search") String search, Pageable pageable);

    boolean existsByNomeIgnoreCase(String nome);
}
