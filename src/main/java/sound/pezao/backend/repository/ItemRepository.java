package sound.pezao.backend.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.relatorio.dto.ItemCriticoDTO;
import sound.pezao.backend.relatorio.dto.RelatorioKpiDTO;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    @Query("""
        SELECT i FROM Item i
        WHERE (:ativo IS NULL OR i.ativo = :ativo)
        AND (:search IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<Item> findAllFiltered(
            @Param("ativo") Boolean ativo,
            @Param("search") String search,
            Pageable pageable
    );

    boolean existsByNomeIgnoreCase(String nome);

    /**
     * Carrega o item travando a linha até o fim da transação, para que duas
     * movimentações simultâneas não leiam o mesmo saldo e sobrescrevam uma à outra.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.id = :id")
    Optional<Item> findByIdParaMovimentacao(@Param("id") Integer id);

    @Query("""
        SELECT new sound.pezao.backend.relatorio.dto.RelatorioKpiDTO(
            COUNT(i),
            SUM(CASE WHEN i.quantidadeAtual > i.quantidadeMinima THEN 1 ELSE 0 END),
            SUM(CASE WHEN i.quantidadeAtual > 0 AND i.quantidadeAtual <= i.quantidadeMinima THEN 1 ELSE 0 END),
            SUM(CASE WHEN i.quantidadeAtual = 0 THEN 1 ELSE 0 END)
        )
        FROM Item i WHERE i.ativo = true
    """)
    RelatorioKpiDTO buscarKpis();

    @Query("""
        SELECT new sound.pezao.backend.relatorio.dto.ItemCriticoDTO(
            i.nome,
            i.quantidadeAtual,
            i.quantidadeMinima,
            CASE WHEN i.quantidadeAtual = 0 THEN 'ZERADO' ELSE 'BAIXO' END
        )
        FROM Item i
        WHERE i.ativo = true
        AND i.quantidadeAtual <= i.quantidadeMinima
        ORDER BY i.quantidadeAtual ASC
    """)
    List<ItemCriticoDTO> buscarItensCriticos();
}
