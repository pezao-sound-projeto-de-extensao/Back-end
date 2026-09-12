package sound.pezao.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.entities.Arquivo;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.exception.ArquivoInvalidoException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ArquivoRepository;
import sound.pezao.backend.repository.MovimentacaoRepository;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ArmazenamentoArquivoService armazenamento;
    private final ArquivoRepository arquivoRepository;

    public MovimentacaoService(
            MovimentacaoRepository movimentacaoRepository,
            ArmazenamentoArquivoService armazenamento,
            ArquivoRepository arquivoRepository
    ) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.armazenamento = armazenamento;
        this.arquivoRepository = arquivoRepository;
    }

    public Movimentacao salvar(Movimentacao movimentacao) {
        return movimentacaoRepository.save(movimentacao);
    }

    public Movimentacao buscarPorId(Integer id) {
        return movimentacaoRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Movimentação",
                                id
                        )
                );
    }

    public Page listarComFiltros(
            Integer itemId,
            String tipo,
            Integer usuarioId,
            String search,
            LocalDate dataInicio,
            LocalDate dataFim,
            Pageable pageable
    ) {
        return movimentacaoRepository.findWithFilters(
                itemId, tipo, usuarioId, search, dataInicio, dataFim, pageable
        );
    }

    public Movimentacao uploadNota(Integer movimentacaoId, MultipartFile arquivo) {
        Movimentacao movimentacao = buscarPorId(movimentacaoId);

        Optional<Arquivo> arquivoExistente = arquivoRepository
                .findByTabelaOrigemAndRegistroIdAndTipoArquivo("movimentacao", movimentacaoId, "nota_entrada");

        String uriAntiga = arquivoExistente.map(Arquivo::getUri).orElse(null);
        String uriNova = armazenamento.salvar(arquivo, "notas");

        try {
            Arquivo arq = arquivoExistente.orElse(new Arquivo());
            arq.setTabelaOrigem("movimentacao");
            arq.setRegistroId(movimentacaoId);
            arq.setTipoArquivo("nota_entrada");
            arq.setUri(uriNova);
            arq.setNome(arquivo.getOriginalFilename());
            arq.setMimeType(arquivo.getContentType() != null
                    ? arquivo.getContentType()
                    : "application/octet-stream");
            arq.setTamanho(Math.toIntExact(arquivo.getSize()));

            arquivoRepository.save(arq);

            if (uriAntiga != null && !uriAntiga.equals(uriNova)) {
                armazenamento.deletar(uriAntiga);
            }

            return movimentacao;
        } catch (RuntimeException e) {
            armazenamento.deletar(uriNova);
            throw e;
        }
    }

    public Resource baixarNota(Integer movimentacaoId) {
        Arquivo arq = arquivoRepository
                .findByTabelaOrigemAndRegistroIdAndTipoArquivo("movimentacao", movimentacaoId, "nota_entrada")
                .orElseThrow(() -> new ArquivoInvalidoException("A movimentação não possui nota fiscal."));

        return armazenamento.carregar(arq.getUri());
    }

    public void deletarNota(Integer movimentacaoId) {
        Arquivo arq = arquivoRepository
                .findByTabelaOrigemAndRegistroIdAndTipoArquivo("movimentacao", movimentacaoId, "nota_entrada")
                .orElse(null);

        if (arq != null) {
            String uri = arq.getUri();
            arquivoRepository.delete(arq);
            if (uri != null) {
                armazenamento.deletar(uri);
            }
        }
    }

    public void deletar(Movimentacao movimentacao) {
        movimentacaoRepository.delete(movimentacao);
    }
}