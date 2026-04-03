package sound.pezao.backend.dto.alertaDTO;

import io.swagger.v3.oas.annotations.media.Schema;

public record AlertaResponse(
        @Schema(description = "ID do Item", example = "1")
        Integer itemId,

        @Schema(description = "Nome do Item", example = "Módulo amplificador 400W")
        String itemNome,

        @Schema(description = "Quantidade Atual", example = "2")
        Integer quantidadeAtual,

        @Schema(description = "Quantidade Minima antes de gerar alerta", example = "5")
        Integer quantidadeMinima,

        @Schema(description = "Tipo do Alerta", example = "estoque_baixo")
        String tipoAlerta
) {

}
