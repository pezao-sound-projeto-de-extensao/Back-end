package sound.pezao.backend.dto.categoriaDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados enviados para criar ou atualizar uma categoria")
public record CategoriaRequest (
    @Schema(description = "Nome da categoria", example = "Som automotivo")
    @NotBlank(message = "Nome é obrigatório")
    String nome
){}
