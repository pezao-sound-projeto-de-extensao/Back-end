package sound.pezao.backend.dto.movimentacaoDTO;

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
        NotaInfo notaInfo = null;

        if (movimentacao.getUriNotaEntrada() != null) {
            notaInfo = new NotaInfo(
                    "/movimentacoes/" +
                            movimentacao.getId() +
                            "/nota/download",
                    movimentacao.getNomeNotaEntrada(),
                    movimentacao.getMimeTypeNotaEntrada(),
                    movimentacao.getTamanhoNotaEntrada()
            );
        }

        return new MovimentacaoResponse(
                movimentacao.getId(),
                new ItemResumoResponse(
                        movimentacao.getItem().getId(),
                        movimentacao.getItem().getNome(),
                        movimentacao.getItem().getCategoria().getId(),
                        movimentacao.getItem().getCategoria().getNome(),
                        movimentacao.getItem().getUnidade().getId(),
                        movimentacao.getItem().getUnidade().getNome(),
                        movimentacao.getItem().getUnidade().getAbreviacao()
                ),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getEstoqueAntes(),
                movimentacao.getEstoqueDepois(),
                movimentacao.getData(),
                movimentacao.getObservacao(),
                movimentacao.getCriadoEm(),
                notaInfo
        );
    }

    public List<MovimentacaoResponse> toResponseList(List<Movimentacao> movimentacoes) {
        return movimentacoes.stream()
                .map(this::toResponse)
                .toList();
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
}