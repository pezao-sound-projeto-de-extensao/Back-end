package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sound.pezao.backend.entities.Arquivo;

import java.util.Optional;

@Repository
public interface ArquivoRepository extends JpaRepository<Arquivo, Integer> {

    Optional<Arquivo> findByItem_IdAndTipoArquivo(
            Integer itemId,
            String tipoArquivo
    );

    Optional<Arquivo> findByMovimentacao_IdAndTipoArquivo(
            Integer movimentacaoId,
            String tipoArquivo
    );
}
