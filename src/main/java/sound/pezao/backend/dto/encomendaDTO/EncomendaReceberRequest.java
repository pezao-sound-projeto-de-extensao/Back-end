package sound.pezao.backend.dto.encomendaDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados do recebimento de uma encomenda")
public record EncomendaReceberRequest(

        @Schema(description = "Produto do catálogo que receberá a entrada em estoque. "
                + "Obrigatório apenas quando a encomenda foi criada para um produto que ainda "
                + "não estava cadastrado.", example = "1")
        Integer itemId
) {}
