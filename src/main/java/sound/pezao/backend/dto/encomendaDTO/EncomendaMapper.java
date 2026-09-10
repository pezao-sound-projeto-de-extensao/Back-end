package sound.pezao.backend.dto.encomendaDTO;

import sound.pezao.backend.entities.Encomenda;
import sound.pezao.backend.entities.Item;

public class EncomendaMapper {

    public static EncomendaResponse toResponse(Encomenda encomenda) {
        Item item = encomenda.getItem();

        return new EncomendaResponse(
                encomenda.getId(),
                encomenda.getDescricao(),
                fotoUrl(item),
                item != null ? item.getId() : null,
                encomenda.getQuantidade(),
                encomenda.getOrcamento().getId(),
                encomenda.getOrcamento().getCliente().getNome(),
                encomenda.getStatus(),
                encomenda.getCriadoEm(),
                encomenda.getRecebidaEm(),
                encomenda.getConcluidaEm()
        );
    }

    private static String fotoUrl(Item item) {
        if (item == null || item.getUriImagem() == null) {
            return null;
        }
        return "/itens/" + item.getId() + "/imagem/download";
    }
}
