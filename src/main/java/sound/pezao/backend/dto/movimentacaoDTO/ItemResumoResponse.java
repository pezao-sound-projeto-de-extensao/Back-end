package sound.pezao.backend.dto.movimentacaoDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo do item exibido na tabela de movimentações")
public record ItemResumoResponse(
        Integer id,
        String nome,
        Integer categoriaId,
        String categoriaNome,
        Integer unidadeId,
        String unidadeNome,
        String unidadeAbreviacao,

        @Schema(description = "URL da foto do produto — a primeira imagem cadastrada. "
                + "Nulo quando o item não tem imagem.",
                example = "/api/itens/1/imagens/1/download")
        String fotoUrl
) {}
