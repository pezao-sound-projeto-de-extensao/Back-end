package sound.pezao.backend.dto.clienteDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados retornados de um cliente")
public record ClienteResponse(

        @Schema(description = "ID do cliente", example = "1")
        Integer id,

        @Schema(description = "Nome do cliente", example = "João da Silva")
        String nome,

        @Schema(description = "Telefone de contato", example = "(11) 98888-7777")
        String telefone,

        @Schema(description = "Data de cadastro")
        LocalDateTime criadoEm
) {}
