package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sound.pezao.backend.entities.Movimentacao;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Integer> {
    @Query("SELECT m FROM Movimentacao m WHERE " +
            "(:itemId IS NULL OR m.item.id = :itemId) AND " +
            "(:tipo IS NULL OR m.tipo = :tipo) AND " +
            "(:usuarioId IS NULL OR m.usuario.id = :usuarioId) AND " +
            "(:dataInicio IS NULL OR m.data >= :dataInicio) AND " +
            "(:dataFim IS NULL OR m.data <= :dataFim)")
    List<Movimentacao> findWithFilters(
            @Param("itemId") Integer itemId,
            @Param("tipo") String tipo,
            @Param("usuarioId") Integer usuarioId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

}
