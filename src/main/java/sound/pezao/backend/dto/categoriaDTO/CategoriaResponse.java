package sound.pezao.backend.dto.categoriaDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados retornados de uma categoria")
public record CategoriaResponse (
    @Schema(description = "ID da categoria", example = "1")
    Integer id,

    @Schema(description = "Nome da categoria", example = "Som automotivo")
    String nome
){}