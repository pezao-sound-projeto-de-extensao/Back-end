package sound.pezao.backend.dto.itemDTO;

import sound.pezao.backend.entities.Arquivo;
import sound.pezao.backend.entities.Categoria;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.StatusEstoque;
import sound.pezao.backend.entities.Unidade;
import sound.pezao.backend.repository.ArquivoRepository;

import java.util.Optional;

public class ItemMapper {

    public static ItemResponse toResponse(Item item, ArquivoRepository arquivoRepository) {
        ImagemInfo imagemInfo = null;

        Optional<Arquivo> arqOpt = arquivoRepository
                .findByTabelaOrigemAndRegistroIdAndTipoArquivo("item", item.getId(), "imagem");

        if (arqOpt.isPresent()) {
            Arquivo arq = arqOpt.get();
            imagemInfo = new ImagemInfo(
                    "/itens/" + item.getId() + "/imagem/download",
                    arq.getNome(),
                    arq.getMimeType(),
                    arq.getTamanho()
            );
        }

        return new ItemResponse(
                item.getId(),
                item.getNome(),
                item.getCategoria().getId(),
                item.getCategoria().getNome(),
                item.getUnidade().getId(),
                item.getUnidade().getNome(),
                item.getUnidade().getAbreviacao(),
                item.getQuantidadeAtual(),
                item.getQuantidadeMinima(),
                item.getPrecoCusto(),
                item.getPrecoVenda(),
                StatusEstoque.calcular(item.getQuantidadeAtual(), item.getQuantidadeMinima()),
                item.getAtivo(),
                imagemInfo
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