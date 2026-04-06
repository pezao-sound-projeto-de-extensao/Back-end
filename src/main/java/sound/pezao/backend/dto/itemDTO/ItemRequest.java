package sound.pezao.backend.dto.itemDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Schema(description = "Dados para cadastro ou atualização de um item")
public record ItemRequest(
        @Schema(description = "Nome do item", example = "Módulo amplificador 400W")
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @Schema(description = "Quantidade atual em estoque", example = "5")
        @NotNull(message = "Quantidade atual é obrigatória")
        @Min(value = 0, message = "Quantidade atual não pode ser negativa")
        Integer quantidadeAtual,

        @Schema(description = "Quantidade mínima antes de gerar alerta", example = "3")
        @NotNull(message = "Quantidade mínima é obrigatória")
        @Min(value = 0, message = "Quantidade mínima não pode ser negativa")
        Integer quantidadeMinima,

        @Schema(description = "Preço de custo do item", example = "180.00")
        @DecimalMin(value = "0.0", inclusive = true, message = "Preço de custo não pode ser negativo")
        Double precoCusto,

        @Schema(description = "Preço de venda do item", example = "320.00")
        @DecimalMin(value = "0.0", inclusive = true, message = "Preço de venda não pode ser negativo")
        Double precoVenda
) {
}
