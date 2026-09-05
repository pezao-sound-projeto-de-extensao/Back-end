package sound.pezao.backend.dto.movimentacaoDTO;

import sound.pezao.backend.entities.Movimentacao;

public class MovimentacaoMapper {

    public static MovimentacaoResponse toResponse(
            Movimentacao movimentacao
    ) {
        NotaInfo notaInfo = null;

        if (movimentacao.getUriNotaEntrada() != null) {
            notaInfo = new NotaInfo(
                    "/api/movimentacoes/" +
                            movimentacao.getId() +
                            "/nota/download",
                    movimentacao.getNomeNotaEntrada(),
                    movimentacao.getMimeTypeNotaEntrada(),
                    movimentacao.getTamanhoNotaEntrada()
            );
        }

        return new MovimentacaoResponse(
                movimentacao.getId(),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getEstoqueAntes(),
                movimentacao.getEstoqueDepois(),
                movimentacao.getData(),
                movimentacao.getObservacao(),
                notaInfo
        );
    }

    public static Movimentacao toEntity(
            MovimentacaoRequest request
    ) {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setTipo(request.tipo());
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setData(request.data());
        movimentacao.setObservacao(request.observacao());
        return movimentacao;
    }

    public static java.util.List<MovimentacaoResponse> toResponseList(
            java.util.List<Movimentacao> movimentacoes
    ) {
        return movimentacoes.stream()
                .map(MovimentacaoMapper::toResponse)
                .toList();
    }
}