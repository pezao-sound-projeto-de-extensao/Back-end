package sound.pezao.backend.dto.unidadesDTO;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Dados para resposta de uma unidade")
public record UnidadeResponse(
    @Schema(description = "nome da unidade", example = "Unidade")
    String nome,
    @Schema(description = "abreviação da unidade", example = "un")
    String abreviacao) {
}
