package sound.pezao.backend.dto.orcamentoDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import sound.pezao.backend.dto.clienteDTO.ClienteRequest;

import java.util.List;

@Schema(description = "Dados para criação ou edição de um orçamento. Informe clienteId "
        + "para um cliente já cadastrado ou clienteNovo para cadastrar junto — nunca os dois.")
public record OrcamentoRequest(

        @Schema(description = "ID de um cliente já cadastrado", example = "1")
        Integer clienteId,

        @Schema(description = "Dados de um cliente novo, cadastrado junto com o orçamento")
        @Valid
        ClienteRequest clienteNovo,

        @Schema(description = "Observação do orçamento", example = "Entrega prevista para a próxima semana")
        @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
        String observacao,

        @Schema(description = "Itens do orçamento")
        @NotEmpty(message = "O orçamento precisa de pelo menos um item")
        @Valid
        List<OrcamentoItemRequest> itens
) {

    public boolean temClienteExistente() {
        return clienteId != null;
    }

    public boolean temClienteNovo() {
        return clienteNovo != null;
    }
}
