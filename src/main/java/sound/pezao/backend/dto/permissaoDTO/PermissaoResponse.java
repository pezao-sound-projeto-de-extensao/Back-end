package sound.pezao.backend.dto.permissaoDTO;

import io.swagger.v3.oas.annotations.media.Schema;

public record PermissaoResponse(
    @Schema(description = "nome da permissão", example = "cadastrar_itens")
    String nome,
    @Schema(description = "descrição da permissão", example = "Cadastrar novos itens no estoque")
    String descricao
) {
}
