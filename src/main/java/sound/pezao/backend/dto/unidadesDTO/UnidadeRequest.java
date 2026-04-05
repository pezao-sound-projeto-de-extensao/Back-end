package sound.pezao.backend.dto.unidadesDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para criar ou atualizar uma unidade de medida")
public record UnidadeRequest(
    @Schema(description = "Nome da unidade de medida", example = "Unidade")
    @NotBlank(message = "Nome é obrigatório")
    String nome,
    
    @Schema(description = "Abreviação da unidade de medida", example = "un")
    @NotBlank(message = "Abreviação é obrigatória")
    String abreviacao) {
}
