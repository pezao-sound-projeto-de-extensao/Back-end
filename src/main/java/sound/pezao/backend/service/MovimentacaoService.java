package sound.pezao.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.exception.ArquivoInvalidoException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.MovimentacaoRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ArmazenamentoArquivoService armazenamento;

    public MovimentacaoService(
            MovimentacaoRepository movimentacaoRepository,
            ArmazenamentoArquivoService armazenamento
    ) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.armazenamento = armazenamento;
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

    public List<Movimentacao> listarComFiltros(
            Integer itemId,
            String tipo,
            Integer usuarioId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        return movimentacaoRepository.findWithFilters(
                itemId,
                tipo,
                usuarioId,
                dataInicio,
                dataFim
        );
    }

    public Movimentacao uploadNota(
            Integer movimentacaoId,
            MultipartFile arquivo
    ) {
        Movimentacao movimentacao = buscarPorId(movimentacaoId);

        String uriAntiga = movimentacao.getUriNotaEntrada();
        String uriNova = armazenamento.salvar(arquivo, "notas");

        try {
            movimentacao.setUriNotaEntrada(uriNova);
            movimentacao.setNomeNotaEntrada(
                    arquivo.getOriginalFilename()
            );
            movimentacao.setMimeTypeNotaEntrada(
                    arquivo.getContentType() != null
                            ? arquivo.getContentType()
                            : "application/octet-stream"
            );
            movimentacao.setTamanhoNotaEntrada(
                    Math.toIntExact(arquivo.getSize())
            );

            Movimentacao salva =
                    movimentacaoRepository.save(movimentacao);

            if (uriAntiga != null && !uriAntiga.equals(uriNova)) {
                armazenamento.deletar(uriAntiga);
            }

            return salva;
        } catch (RuntimeException e) {
            armazenamento.deletar(uriNova);
            throw e;
        }
    }

    public Resource baixarNota(Integer movimentacaoId) {
        Movimentacao movimentacao = buscarPorId(movimentacaoId);

        if (movimentacao.getUriNotaEntrada() == null) {
            throw new ArquivoInvalidoException(
                    "A movimentação não possui nota fiscal."
            );
        }

        return armazenamento.carregar(
                movimentacao.getUriNotaEntrada()
        );
    }

    public void deletarNota(Integer movimentacaoId) {
        Movimentacao movimentacao = buscarPorId(movimentacaoId);
        String uri = movimentacao.getUriNotaEntrada();

        movimentacao.setUriNotaEntrada(null);
        movimentacao.setNomeNotaEntrada(null);
        movimentacao.setMimeTypeNotaEntrada(null);
        movimentacao.setTamanhoNotaEntrada(null);

        movimentacaoRepository.save(movimentacao);

        if (uri != null) {
            armazenamento.deletar(uri);
        }
    }

    public void deletar(Movimentacao movimentacao) {
        movimentacaoRepository.delete(movimentacao);
    }
}