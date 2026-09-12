package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sound.pezao.backend.entities.Arquivo;

import java.util.Optional;

@Repository
public interface ArquivoRepository extends JpaRepository<Arquivo, Integer> {

    Optional<Arquivo> findByTabelaOrigemAndRegistroIdAndTipoArquivo(
            String tabelaOrigem,
            Integer registroId,
            String tipoArquivo
    );

    void deleteByTabelaOrigemAndRegistroIdAndTipoArquivo(
            String tabelaOrigem,
            Integer registroId,
            String tipoArquivo
    );
}