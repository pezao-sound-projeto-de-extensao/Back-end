package sound.pezao.backend.dto.orcamentoDTO;

import sound.pezao.backend.dto.clienteDTO.ClienteMapper;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Orcamento;
import sound.pezao.backend.entities.OrcamentoItem;

import java.util.List;

public class OrcamentoMapper {

    public static OrcamentoResponse toResponse(Orcamento orcamento) {
        List<OrcamentoItemResponse> itens = orcamento.getItens().stream()
                .map(OrcamentoMapper::toItemResponse)
                .toList();

        return new OrcamentoResponse(
                orcamento.getId(),
                ClienteMapper.toResponse(orcamento.getCliente()),
                orcamento.getStatus(),
                itens.size(),
                orcamento.getValorTotal(),
                orcamento.getObservacao(),
                orcamento.getCriadoEm(),
                itens
        );
    }

    public static OrcamentoItemResponse toItemResponse(OrcamentoItem item) {
        Item produto = item.getItem();

        return new OrcamentoItemResponse(
                item.getId(),
                produto != null ? produto.getId() : null,
                item.getDescricao(),
                fotoUrl(produto),
                item.isProdutoNovo(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.calcularSubtotal()
        );
    }

    private static String fotoUrl(Item produto) {
        if (produto == null || produto.getUriImagem() == null) {
            return null;
        }
        return "/itens/" + produto.getId() + "/imagem/download";
    }
}
