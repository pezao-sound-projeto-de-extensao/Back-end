package sound.pezao.backend.dto.orcamentoDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import sound.pezao.backend.dto.clienteDTO.ClienteResponse;
import sound.pezao.backend.entities.StatusOrcamento;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Dados retornados de um orçamento")
public record OrcamentoResponse(

        @Schema(description = "ID do orçamento, usado também como número", example = "1")
        Integer id,

        @Schema(description = "Cliente do orçamento")
        ClienteResponse cliente,

        @Schema(description = "Situação do orçamento", example = "PENDENTE",
                allowableValues = {"PENDENTE", "ACEITO", "REJEITADO", "CONCLUIDO"})
        StatusOrcamento status,

        @Schema(description = "Quantidade de itens do orçamento", example = "3")
        int quantidadeItens,

        @Schema(description = "Soma dos subtotais dos itens", example = "1280.00")
        Double valorTotal,

        @Schema(description = "Observação", example = "Entrega prevista para a próxima semana")
        String observacao,

        @Schema(description = "Data de criação")
        LocalDateTime criadoEm,

        @Schema(description = "Itens do orçamento")
        List<OrcamentoItemResponse> itens
) {}
