package sound.pezao.backend.dto.categoriaDTO;

import io.swagger.v3.oas.annotations.media.Schema;

public record CategoriaRequest(
    @Schema(description = "Nome da categoria", example = "Som automotivo")
    String name
) {}
