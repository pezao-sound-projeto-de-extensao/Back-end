package sound.pezao.backend.dto.itemDTO;

import io.swagger.v3.oas.annotations.media.Schema;

public record ItemResponse(
        @Schema(description = "ID do item")
        Integer id,

        @Schema(description = "Nome do item")
        String nome,

        @Schema(description = "Quantidade atual em estoque")
        Integer quantidadeAtual,

        @Schema(description = "Quantidade mínima em estoque")
        Integer quantidadeMinima,

        @Schema(description = "Preço de custo")
        Double precoCusto,

        @Schema(description = "Preço de venda")
        Double precoVenda,

        @Schema(description = "Item está ativo?")
        Boolean ativo,

        @Schema(description = "Dados da imagem do item")
        ImagemInfo imagem
) {}