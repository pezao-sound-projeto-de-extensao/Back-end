package sound.pezao.backend.dto.itemDTO;

import sound.pezao.backend.entities.Item;

public class ItemMapper {

    public static ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getNome(),
                item.getQuantidadeAtual(),
                item.getQuantidadeMinima(),
                item.getPrecoCusto(),
                item.getPrecoVenda(),
                item.getAtivo()
        );
    }

    public static Item toEntity(ItemRequest request) {
        Item item = new Item();
        item.setNome(request.nome());
        item.setQuantidadeAtual(request.quantidadeAtual());
        item.setQuantidadeMinima(request.quantidadeMinima());
        item.setPrecoCusto(request.precoCusto());
        item.setPrecoVenda(request.precoVenda());
        return item;
    }
}