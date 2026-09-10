package sound.pezao.backend.dto.clienteDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de um cliente")
public record ClienteRequest(

        @Schema(description = "Nome do cliente", example = "João da Silva")
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @Schema(description = "Telefone de contato", example = "(11) 98888-7777")
        @Size(max = 30, message = "Telefone deve ter no máximo 30 caracteres")
        String telefone
) {}
