package sound.pezao.backend.dto.itemDTO;

import sound.pezao.backend.dto.imagemProdutoDTO.ImagemProdutoMapper;
import sound.pezao.backend.entities.Categoria;
import sound.pezao.backend.entities.ImagemProduto;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Unidade;

import java.util.List;

public class ItemMapper {

    public static ItemResponse toResponse(Item item) {
        return toResponse(item, List.of());
    }

    public static ItemResponse toResponse(Item item, List<ImagemProduto> imagens) {
        return new ItemResponse(
                item.getId(),
                item.getNome(),
                item.getCategoria().getId(),
                item.getCategoria().getNome(),
                item.getQuantidadeAtual(),
                item.getQuantidadeMinima(),
                item.getPrecoCusto(),
                item.getPrecoVenda(),
                item.getAtivo(),
                ImagemProdutoMapper.toResponseList(imagens)
        );
    }

    public static Item toEntity(ItemRequest request, Categoria categoria, Unidade unidade) {
        Item item = new Item();
        item.setNome(request.nome());
        item.setCategoria(categoria);
        item.setUnidade(unidade);
        item.setQuantidadeAtual(request.quantidadeAtual());
        item.setQuantidadeMinima(request.quantidadeMinima());
        item.setPrecoCusto(request.precoCusto());
        item.setPrecoVenda(request.precoVenda());
        return item;
    }
}