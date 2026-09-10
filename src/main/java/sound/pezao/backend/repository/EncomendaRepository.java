package sound.pezao.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sound.pezao.backend.entities.Encomenda;
import sound.pezao.backend.entities.StatusEncomenda;

import java.time.LocalDateTime;
import java.util.List;

public interface EncomendaRepository extends JpaRepository<Encomenda, Integer> {

    /**
     * Listagem filtrada por item ou cliente (mesmo campo de busca) e por status,
     * da encomenda mais recente para a mais antiga.
     */
    @Query(value = """
            SELECT e FROM Encomenda e
            JOIN e.orcamento o
            JOIN o.cliente c
            WHERE (:status IS NULL OR e.status = :status)
            AND (:search IS NULL
                OR LOWER(e.descricao) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY e.criadoEm DESC, e.id DESC
            """,
            countQuery = """
            SELECT COUNT(e) FROM Encomenda e
            JOIN e.orcamento o
            JOIN o.cliente c
            WHERE (:status IS NULL OR e.status = :status)
            AND (:search IS NULL
                OR LOWER(e.descricao) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Encomenda> findAllFiltered(
            @Param("search") String search,
            @Param("status") StatusEncomenda status,
            Pageable pageable
    );

    long countByStatus(StatusEncomenda status);

    long countByStatusAndConcluidaEmBetween(StatusEncomenda status,
                                            LocalDateTime inicio,
                                            LocalDateTime fim);

    List<Encomenda> findByOrcamento_Id(Integer orcamentoId);
}
