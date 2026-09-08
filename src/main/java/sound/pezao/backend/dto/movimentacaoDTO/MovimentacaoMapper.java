package sound.pezao.backend.dto.movimentacaoDTO;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.TipoMovimentacao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class MovimentacaoMapper {

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

    public List<MovimentacaoResponse> toResponseList(List<Movimentacao> movimentacoes) {
        return movimentacoes.stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<MovimentacaoResponse> toResponsePage(Page<Movimentacao> movimentacoes) {
        return movimentacoes.map(this::toResponse);
    }

    public Movimentacao toEntity(MovimentacaoRequest request, Item item) {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setItem(item);
        movimentacao.setTipo(TipoMovimentacao.fromValor(request.tipo()).getValor());
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setData(request.data() != null ? request.data() : LocalDate.now());
        movimentacao.setObservacao(request.observacao());
        movimentacao.setCriadoEm(LocalDateTime.now());

        return movimentacao;
    }

    private ItemResumoResponse montarItem(Item item) {
        return new ItemResumoResponse(
                item.getId(),
                item.getNome(),
                item.getCategoria().getId(),
                item.getCategoria().getNome(),
                item.getUnidade().getId(),
                item.getUnidade().getNome(),
                item.getUnidade().getAbreviacao(),
                // a tabela de movimentações exibe a foto do produto
                item.getUriImagem() != null
                        ? "/itens/" + item.getId() + "/imagem/download"
                        : null
        );
    }

    private NotaInfo montarNota(Movimentacao movimentacao) {
        if (movimentacao.getUriNotaEntrada() == null) {
            return null;
        }

        return new NotaInfo(
                "/movimentacoes/" + movimentacao.getId() + "/nota/download",
                movimentacao.getNomeNotaEntrada(),
                movimentacao.getMimeTypeNotaEntrada(),
                movimentacao.getTamanhoNotaEntrada()
        );
    }
}
