package sound.pezao.backend.dto.alertaDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record AlertaResponse(
        @Schema(description = "ID do Item", example = "1")
        Integer itemId,

        @Schema(description = "Nome do Item", example = "Módulo amplificador 400W")
        String itemNome,

        @Schema(description = "Nome da Categoria", example = "Som Automotivo")
        String categoriaNome,

        @Schema(description = "Unidade de Medida", example = "UN")
        String unidadeMedida,

        @Schema(description = "Quantidade Atual em estoque", example = "2")
        Integer quantidadeAtual,

        @Schema(description = "Quantidade Mínima configurada", example = "5")
        Integer quantidadeMinima,

        @Schema(description = "Tipo do Alerta (zerado ou estoque_baixo)", example = "estoque_baixo")
        String tipoAlerta
) {
}