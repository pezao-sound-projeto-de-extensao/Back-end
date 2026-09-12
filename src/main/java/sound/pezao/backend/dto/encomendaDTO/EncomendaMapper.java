package sound.pezao.backend.dto.encomendaDTO;

import org.springframework.stereotype.Component;
import sound.pezao.backend.entities.Encomenda;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.repository.ArquivoRepository;

@Component
public class EncomendaMapper {

    private final ArquivoRepository arquivoRepository;

    public EncomendaMapper(ArquivoRepository arquivoRepository) {
        this.arquivoRepository = arquivoRepository;
    }

    public EncomendaResponse toResponse(Encomenda encomenda) {
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

    private String fotoUrl(Item item) {
        if (item == null) {
            return null;
        }

        boolean possuiImagem = arquivoRepository
                .findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                        "item",
                        item.getId(),
                        "imagem"
                )
                .isPresent();

        return possuiImagem
                ? "/itens/" + item.getId() + "/imagem/download"
                : null;
    }
}