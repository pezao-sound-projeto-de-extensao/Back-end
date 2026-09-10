package sound.pezao.backend.dto.orcamentoDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Item de um orçamento")
public record OrcamentoItemResponse(

        @Schema(description = "ID do item do orçamento", example = "1")
        Integer id,

        @Schema(description = "ID do produto no catálogo. Nulo quando é um produto novo.", example = "1")
        Integer itemId,

        @Schema(description = "Nome do produto no orçamento", example = "Módulo Amplificador 400W")
        String descricao,

        @Schema(description = "URL da foto do produto. Nula para produto novo ou sem imagem.",
                example = "/itens/1/imagem/download")
        String fotoUrl,

        @Schema(description = "Indica um produto que ainda não existe no catálogo", example = "false")
        boolean produtoNovo,

        @Schema(description = "Quantidade orçada", example = "2")
        Integer quantidade,

        @Schema(description = "Preço unitário", example = "320.00")
        Double precoUnitario,

        @Schema(description = "Quantidade multiplicada pelo preço unitário", example = "640.00")
        Double subtotal
) {}
