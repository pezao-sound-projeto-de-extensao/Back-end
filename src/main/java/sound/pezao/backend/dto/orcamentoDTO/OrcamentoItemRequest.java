package sound.pezao.backend.dto.orcamentoDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Item de um orçamento. Informe itemId para produto do catálogo "
        + "ou descricao para um produto ainda não cadastrado.")
public record OrcamentoItemRequest(

        @Schema(description = "ID do produto cadastrado. Nulo para produto novo.", example = "1")
        Integer itemId,

        @Schema(description = "Nome do produto novo. Obrigatório quando itemId não é informado.",
                example = "Kit de fiação 4mm")
        String descricao,

        @Schema(description = "Quantidade orçada", example = "2")
        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Integer quantidade,

        @Schema(description = "Preço unitário praticado no orçamento", example = "320.00")
        @NotNull(message = "Preço unitário é obrigatório")
        @PositiveOrZero(message = "Preço unitário não pode ser negativo")
        Double precoUnitario
) {

    public boolean temProdutoDoCatalogo() {
        return itemId != null;
    }

    public boolean temDescricao() {
        return descricao != null && !descricao.isBlank();
    }
}
