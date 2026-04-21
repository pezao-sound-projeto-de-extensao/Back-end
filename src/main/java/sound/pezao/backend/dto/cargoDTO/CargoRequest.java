package sound.pezao.backend.dto.cargoDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CargoRequest(
    @Schema(description = "Nome do cargo", example = "Supervisor")
    @NotBlank(message = "Nome é obrigatório")
    String nome,
    @Schema(description = "Descrição do cargo", example = "Acesso a relatórios e movimentações")
    @NotBlank(message = "Descrição é obrigatória")
    String descricao,
    @Schema(description = "Permissões do cargo", example = "[\"registrar_entrada\", \"registrar_saida\", \"ver_relatorios\"]")
    @NotEmpty
    Set<String> permissoes
) {
}
