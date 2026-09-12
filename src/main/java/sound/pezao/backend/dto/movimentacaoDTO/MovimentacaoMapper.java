package sound.pezao.backend.dto.movimentacaoDTO;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import sound.pezao.backend.entities.Arquivo;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.repository.ArquivoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class MovimentacaoMapper {

    private final ArquivoRepository arquivoRepository;

    public MovimentacaoMapper(ArquivoRepository arquivoRepository) {
        this.arquivoRepository = arquivoRepository;
    }

    public MovimentacaoResponse toResponse(Movimentacao movimentacao) {
        return new MovimentacaoResponse(
                movimentacao.getId(),
                montarItem(movimentacao.getItem()),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getEstoqueAntes(),
                movimentacao.getEstoqueDepois(),
                movimentacao.getData(),
                movimentacao.getObservacao(),
                movimentacao.getCriadoEm(),
                montarNota(movimentacao)
        );
    }

    public List<MovimentacaoResponse> toResponseList(
            List<Movimentacao> movimentacoes
    ) {
        return movimentacoes.stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<MovimentacaoResponse> toResponsePage(
            Page<Movimentacao> movimentacoes
    ) {
        return movimentacoes.map(this::toResponse);
    }

    public Movimentacao toEntity(MovimentacaoRequest request, Item item) {
        Movimentacao movimentacao = new Movimentacao();

        movimentacao.setItem(item);
        movimentacao.setTipo(
                TipoMovimentacao.fromValor(request.tipo()).getValor()
        );
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setData(
                request.data() != null ? request.data() : LocalDate.now()
        );
        movimentacao.setObservacao(request.observacao());
        movimentacao.setCriadoEm(LocalDateTime.now());

        return movimentacao;
    }

    private ItemResumoResponse montarItem(Item item) {
        Optional<Arquivo> arqOpt = arquivoRepository
                .findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                        "item",
                        item.getId(),
                        "imagem"
                );

        String urlImagem = arqOpt.isPresent()
                ? "/itens/" + item.getId() + "/imagem/download"
                : null;

        return new ItemResumoResponse(
                item.getId(),
                item.getNome(),
                item.getCategoria().getId(),
                item.getCategoria().getNome(),
                item.getUnidade().getId(),
                item.getUnidade().getNome(),
                item.getUnidade().getAbreviacao(),
                urlImagem
        );
    }

    private NotaInfo montarNota(Movimentacao movimentacao) {
        Optional<Arquivo> arqOpt = arquivoRepository
                .findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                        "movimentacao",
                        movimentacao.getId(),
                        "nota_entrada"
                );

        if (arqOpt.isEmpty()) {
            return null;
        }

        Arquivo arq = arqOpt.get();

        return new NotaInfo(
                "/movimentacoes/" + movimentacao.getId() + "/nota/download",
                arq.getNome(),
                arq.getMimeType(),
                arq.getTamanho()
        );
    }
}