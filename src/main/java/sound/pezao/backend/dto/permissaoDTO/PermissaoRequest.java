package sound.pezao.backend.dto.permissaoDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PermissaoRequest(
    @Schema(description = "Identificador da permissão", example = "cadastrar_itens")
    @NotBlank String nome,

    @Schema(description = "O que essa permissão permite fazer", example = "Cadastrar novos itens no estoque")
    @NotBlank String descricao
) {
}
