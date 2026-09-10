package sound.pezao.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sound.pezao.backend.entities.Orcamento;
import sound.pezao.backend.entities.StatusOrcamento;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Integer> {

    /**
     * Listagem filtrada por cliente ou número do orçamento e por status. O número
     * chega separado do texto porque a busca aceita as duas coisas no mesmo campo:
     * quando o usuário digita algo numérico, o service preenche :numero.
     */
    @Query(value = """
            SELECT o FROM Orcamento o
            JOIN o.cliente c
            WHERE (:status IS NULL OR o.status = :status)
            AND (:search IS NULL
                OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :search, '%'))
                OR o.id = :numero)
            ORDER BY o.criadoEm DESC, o.id DESC
            """,
            countQuery = """
            SELECT COUNT(o) FROM Orcamento o
            JOIN o.cliente c
            WHERE (:status IS NULL OR o.status = :status)
            AND (:search IS NULL
                OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :search, '%'))
                OR o.id = :numero)
            """)
    Page<Orcamento> findAllFiltered(
            @Param("search") String search,
            @Param("numero") Integer numero,
            @Param("status") StatusOrcamento status,
            Pageable pageable
    );
}
