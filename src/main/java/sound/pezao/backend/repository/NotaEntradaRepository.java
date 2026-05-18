package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sound.pezao.backend.entities.NotaEntrada;

import java.util.Collection;
import java.util.List;

public interface NotaEntradaRepository extends JpaRepository<NotaEntrada, Integer> {

    List<NotaEntrada> findByMovimentacao_Id(Integer movimentacaoId);

    List<NotaEntrada> findByMovimentacao_IdIn(Collection<Integer> movimentacaoIds);
}
