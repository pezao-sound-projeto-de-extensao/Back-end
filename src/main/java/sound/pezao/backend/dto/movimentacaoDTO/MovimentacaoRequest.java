package sound.pezao.backend.dto.movimentacaoDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "Dados para cadastrar uma movimentacao")
public record MovimentacaoRequest(

        @Schema(description = "ID do Item da Movimentacao")
        @NotNull(message = "O ID do item é obrigatório")
        Integer itemId,

        @Schema(description = "Tipo da Movimentacao")
        @NotBlank(message = "O tipo de movimentação não pode estar vazio")
        String tipo,

        @Schema(description = "Quantidade da Movimentacao")
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade,

        @Schema(description = "Data da Movimentacao")
        @NotNull(message = "A data é obrigatória")
        LocalDate data,

        @Schema(description = "Observacao da Movimentacao")
        String observacao
) {
}