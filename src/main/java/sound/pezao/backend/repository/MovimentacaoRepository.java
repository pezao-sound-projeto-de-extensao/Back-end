package sound.pezao.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.relatorio.dto.ItemMaisMovimentadoDTO;
import sound.pezao.backend.relatorio.dto.MovimentacaoHistoricoDTO;

import java.time.LocalDate;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Integer> {

    @Query(
            value = """
                    SELECT m
                    FROM Movimentacao m
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
                    SELECT COUNT(m)
                    FROM Movimentacao m
                    JOIN m.item i
                    WHERE (:itemId IS NULL OR i.id = :itemId)
                    AND (:tipo IS NULL OR m.tipo = :tipo)
                    AND (:usuarioId IS NULL OR m.usuario.id = :usuarioId)
                    AND (:search IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :search, '%')))
                    AND (:dataInicio IS NULL OR m.data >= :dataInicio)
                    AND (:dataFim IS NULL OR m.data <= :dataFim)
                    """
    )
    Page<Movimentacao> findWithFilters(
            @Param("itemId") Integer itemId,
            @Param("tipo") TipoMovimentacao tipo,
            @Param("usuarioId") Integer usuarioId,
            @Param("search") String search,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            Pageable pageable
    );

    @Query(
            value = """
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
                    JOIN m.item i
                    WHERE m.data BETWEEN :dataInicio AND :dataFim
                    AND (:categoriaId IS NULL OR i.categoria.id = :categoriaId)
                    ORDER BY m.data DESC
                    """,
            countQuery = """
                    SELECT COUNT(m)
                    FROM Movimentacao m
                    JOIN m.item i
                    WHERE m.data BETWEEN :dataInicio AND :dataFim
                    AND (:categoriaId IS NULL OR i.categoria.id = :categoriaId)
                    """
    )
    Page<MovimentacaoHistoricoDTO> buscarHistorico(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("categoriaId") Integer categoriaId,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT new sound.pezao.backend.relatorio.dto.ItemMaisMovimentadoDTO(
                        i.nome,
                        COUNT(m)
                    )
                    FROM Movimentacao m
                    JOIN m.item i
                    WHERE m.data BETWEEN :dataInicio AND :dataFim
                    AND (:categoriaId IS NULL OR i.categoria.id = :categoriaId)
                    GROUP BY i.id, i.nome
                    ORDER BY COUNT(m) DESC
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT i.id)
                    FROM Movimentacao m
                    JOIN m.item i
                    WHERE m.data BETWEEN :dataInicio AND :dataFim
                    AND (:categoriaId IS NULL OR i.categoria.id = :categoriaId)
                    """
    )
    Page<ItemMaisMovimentadoDTO> buscarMaisMovimentados(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("categoriaId") Integer categoriaId,
            Pageable pageable
    );
}