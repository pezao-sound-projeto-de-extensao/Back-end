package sound.pezao.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.dto.arquivoDTO.ArquivoDownload;
import sound.pezao.backend.dto.notaEntradaDTO.NotaEntradaMapper;
import sound.pezao.backend.dto.notaEntradaDTO.NotaEntradaResponse;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.NotaEntrada;
import sound.pezao.backend.exception.ArquivoInvalidoException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.MovimentacaoRepository;
import sound.pezao.backend.repository.NotaEntradaRepository;

import java.util.List;
import java.util.Set;

@Service
public class NotaEntradaService {

    private static final String SUBPASTA = "notas-entrada";
    private static final String MIME_PADRAO = "application/octet-stream";
    private static final Set<String> TIPOS_VALIDOS = Set.of("imagem", "nota_fiscal");

    private final NotaEntradaRepository repository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final ArmazenamentoArquivoService armazenamento;

    public NotaEntradaService(NotaEntradaRepository repository,
                              MovimentacaoRepository movimentacaoRepository,
                              ArmazenamentoArquivoService armazenamento) {
        this.repository = repository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.armazenamento = armazenamento;
    }

    public NotaEntradaResponse upload(Integer movimentacaoId, MultipartFile arquivo, String tipo) {
        if (tipo == null || !TIPOS_VALIDOS.contains(tipo)) {
            throw new ArquivoInvalidoException("Tipo inválido. Use 'imagem' ou 'nota_fiscal'.");
        }

        Movimentacao movimentacao = movimentacaoRepository.findById(movimentacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Movimentação", movimentacaoId));

        String caminho = armazenamento.salvar(arquivo, SUBPASTA);
        try {
            NotaEntrada nota = new NotaEntrada();
            nota.setMovimentacao(movimentacao);
            nota.setTipo(tipo);
            nota.setNomeArquivo(arquivo.getOriginalFilename());
            nota.setCaminho(caminho);
            nota.setMimeType(arquivo.getContentType() != null ? arquivo.getContentType() : MIME_PADRAO);
            nota.setTamanhoBytes((int) arquivo.getSize());
            return NotaEntradaMapper.toResponse(repository.save(nota));
        } catch (RuntimeException e) {
            armazenamento.deletar(caminho);
            throw e;
        }
    }

    public List<NotaEntradaResponse> listar(Integer movimentacaoId) {
        garantirMovimentacaoExiste(movimentacaoId);
        return NotaEntradaMapper.toResponseList(repository.findByMovimentacao_Id(movimentacaoId));
    }

    public ArquivoDownload baixar(Integer movimentacaoId, Integer notaId) {
        NotaEntrada nota = buscarDaMovimentacao(movimentacaoId, notaId);
        return new ArquivoDownload(
                armazenamento.carregar(nota.getCaminho()),
                nota.getNomeArquivo(),
                nota.getMimeType()
        );
    }

    public void deletar(Integer movimentacaoId, Integer notaId) {
        NotaEntrada nota = buscarDaMovimentacao(movimentacaoId, notaId);
        repository.delete(nota);
        armazenamento.deletar(nota.getCaminho());
    }

    private void garantirMovimentacaoExiste(Integer movimentacaoId) {
        if (!movimentacaoRepository.existsById(movimentacaoId)) {
            throw new EntityNotFoundException("Movimentação", movimentacaoId);
        }
    }

    private NotaEntrada buscarDaMovimentacao(Integer movimentacaoId, Integer notaId) {
        NotaEntrada nota = repository.findById(notaId)
                .orElseThrow(() -> new EntityNotFoundException("Nota de entrada", notaId));
        if (!nota.getMovimentacao().getId().equals(movimentacaoId)) {
            throw new EntityNotFoundException("Nota de entrada", notaId);
        }
        return nota;
    }
}
