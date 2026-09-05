package sound.pezao.backend.dto.movimentacaoDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MovimentacaoRequest (
        @NotNull
        @Schema(description = "ID do item", example = "1")
        Integer itemId,

        @NotBlank
        @Pattern(regexp = "(?i)entrada|saida", message = "Tipo deve ser 'entrada' ou 'saida'")
        @Schema(description = "Tipo da movimentação", example = "entrada", allowableValues = {"entrada", "saida"})
        String tipo,

        @NotNull
        @Positive(message = "Quantidade deve ser maior que zero")
        @Schema(description = "Quantidade movimentada", example = "5")
        Integer quantidade,

        @Schema(description = "Data da movimentação", example = "2026-04-19")
        LocalDate data,

        @Schema(description = "Observação opcional", example = "Compra fornecedor João, NF 1042")
        String observacao
){
}