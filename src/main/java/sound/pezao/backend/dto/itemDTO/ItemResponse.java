package sound.pezao.backend.dto.itemDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import sound.pezao.backend.dto.imagemProdutoDTO.ImagemProdutoResponse;

import java.util.List;

@Schema(description = "Dados retornados de um item")
public record ItemResponse(

        @Schema(description = "ID do item", example = "1")
        Integer id,

        @Schema(description = "Nome do item", example = "Módulo amplificador 400W")
        String nome,

        @Schema(description = "Quantidade atual em estoque", example = "5")
        Integer quantidadeAtual,

        @Schema(description = "Quantidade mínima antes de gerar alerta", example = "3")
        Integer quantidadeMinima,

        @Schema(description = "Preço de custo do item", example = "180.00")
        Double precoCusto,

        @Schema(description = "Preço de venda do item", example = "320.00")
        Double precoVenda,

        @Schema(description = "Item ativo ou inativo", example = "true")
        Boolean ativo,

        @Schema(description = "Imagens do item")
        List<ImagemProdutoResponse> imagens

) {}