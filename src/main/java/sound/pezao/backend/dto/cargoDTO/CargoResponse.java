package sound.pezao.backend.dto.cargoDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CargoResponse(
    @Schema(description = "ID do cargo", example = "1")
    Integer id,
    @Schema(description = "Nome do cargo", example = "Supervisor")
    String nome,
    @Schema(description = "Descrição do cargo", example = "Acesso a relatórios e movimentações")
    String descricao,
    @Schema(description = "Permissões do cargo", example = "[\"registrar_entrada\", \"registrar_saida\", \"ver_relatorios\"]")
    Set<String> permissoes
) {
}
