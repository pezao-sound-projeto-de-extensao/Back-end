package sound.pezao.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.relatorio.dto.ItemMaisMovimentadoDTO;
import sound.pezao.backend.relatorio.dto.MovimentacaoHistoricoDTO;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Integer> {
    /**
     * Histórico filtrado. As datas são LocalDate porque a coluna é DATE: comparar
     * com LocalDateTime fazia o filtro de período nunca casar corretamente.
     * A ordenação padrão é da movimentação mais recente para a mais antiga.
     */
    @Query(value = """
            SELECT m FROM Movimentacao m
            JOIN m.item i
            WHERE (:itemId IS NULL OR i.id = :itemId)
            AND (:tipo IS NULL OR m.tipo = :tipo)
            AND (:usuarioId IS NULL OR m.usuario.id = :usuarioId)
            AND (:search IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:dataInicio IS NULL OR m.data >= :dataInicio)
            AND (:dataFim IS NULL OR m.data <= :dataFim)
            ORDER BY m.data DESC, m.criadoEm DESC, m.id DESC
            """,
            countQuery = """
            SELECT COUNT(m) FROM Movimentacao m
            JOIN m.item i
            WHERE (:itemId IS NULL OR i.id = :itemId)
            AND (:tipo IS NULL OR m.tipo = :tipo)
            AND (:usuarioId IS NULL OR m.usuario.id = :usuarioId)
            AND (:search IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:dataInicio IS NULL OR m.data >= :dataInicio)
            AND (:dataFim IS NULL OR m.data <= :dataFim)
            """)
    Page<Movimentacao> findWithFilters(
            @Param("itemId") Integer itemId,
            @Param("tipo") String tipo,
            @Param("usuarioId") Integer usuarioId,
            @Param("search") String search,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            Pageable pageable
    );

    @Query("""
    SELECT new sound.pezao.backend.relatorio.dto.MovimentacaoHistoricoDTO(
        m.data,
        i.nome,
        i.categoria.nome,
        m.tipo,
        m.quantidade,
        m.estoqueAntes,
        m.estoqueDepois,
        m.observacao
    )
    FROM Movimentacao m
    JOIN Item i ON i.id = m.item.id
    WHERE m.data BETWEEN :dataInicio AND :dataFim
    AND (:categoriaId IS NULL OR i.categoria.id = :categoriaId)
    ORDER BY m.data DESC
""")
    Page<MovimentacaoHistoricoDTO> buscarHistorico(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("categoriaId") Integer categoriaId,
            Pageable pageable
    );

    @Query("""
    SELECT new sound.pezao.backend.relatorio.dto.ItemMaisMovimentadoDTO(
        i.nome,
        COUNT(m)
    )
    FROM Movimentacao m
    JOIN Item i ON i.id = m.item.id
    WHERE m.data BETWEEN :dataInicio AND :dataFim
    AND (:categoriaId IS NULL OR i.categoria.id = :categoriaId)
    GROUP BY i.id, i.nome
    ORDER BY COUNT(m) DESC
""")
    Page<ItemMaisMovimentadoDTO> buscarMaisMovimentados(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("categoriaId") Integer categoriaId,
            Pageable pageable
    );
}

