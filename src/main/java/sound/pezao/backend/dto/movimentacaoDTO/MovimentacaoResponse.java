package sound.pezao.backend.dto.movimentacaoDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MovimentacaoResponse(
        @Schema(description = "ID da movimentação")
        Integer id,

        @Schema(description = "Dados do item")
        ItemResumoResponse item,

        @Schema(description = "Tipo: entrada ou saida")
        String tipo,

        @Schema(description = "Quantidade movimentada")
        Integer quantidade,

        @Schema(description = "Estoque antes da movimentação")
        Integer estoqueAntes,

        @Schema(description = "Estoque depois da movimentação")
        Integer estoqueDepois,

        @Schema(description = "Data da movimentação")
        LocalDate data,

        @Schema(description = "Observação")
        String observacao,

        @Schema(description = "Data de criação")
        LocalDateTime criadoEm,

        @Schema(description = "Dados da nota fiscal")
        NotaInfo nota
) {}