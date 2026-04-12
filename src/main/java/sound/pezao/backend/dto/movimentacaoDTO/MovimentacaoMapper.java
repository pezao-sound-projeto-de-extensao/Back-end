package sound.pezao.backend.dto.movimentacaoDTO;

import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;

public class MovimentacaoMapper {

    public Movimentacao toEntity(MovimentacaoRequest request) {
        if (request == null) return null;

        Item item = new Item();
        item.setId(request.itemId());

        Movimentacao entity = new Movimentacao();
        entity.setItem(item);
        entity.setTipo(request.tipo());
        entity.setQuantidade(request.quantidade());
        entity.setData(request.data());
        entity.setObservacao(request.observacao());

        return entity;
    }

    public MovimentacaoResponse toResponse(Movimentacao entity) {
        if (entity == null) return null;

        return new MovimentacaoResponse(
                entity.getId(),
                new MovimentacaoResponse.ItemMovimentacao(
                        entity.getItem().getId(),
                        entity.getItem().getNome()
                ),
                new MovimentacaoResponse.UsuarioMovimentacao(
                        entity.getUsuario().getId(),
                        entity.getUsuario().getNome(),
                        entity.getUsuario().getEmail(),
                        new MovimentacaoResponse.CargoUsuarioMovimentacao(
                                entity.getUsuario().getCargo().getId(),
                                entity.getUsuario().getCargo().getNome()
                        )
                ),
                entity.getTipo(),
                entity.getQuantidade(),
                entity.getEstoqueAntes(),
                entity.getEstoqueDepois(),
                entity.getData(),
                entity.getObservacao(),
                entity.getCriadoEm()
        );
    }
}