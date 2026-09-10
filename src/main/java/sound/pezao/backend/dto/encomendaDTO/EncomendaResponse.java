package sound.pezao.backend.dto.encomendaDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import sound.pezao.backend.entities.StatusEncomenda;

import java.time.LocalDateTime;

@Schema(description = "Encomenda gerada pelo aceite de um orçamento")
public record EncomendaResponse(

        @Schema(description = "ID da encomenda", example = "1")
        Integer id,

        @Schema(description = "Nome do item encomendado", example = "Módulo Amplificador 400W")
        String descricao,

        @Schema(description = "URL da foto do item. Nula enquanto o produto não estiver no catálogo.",
                example = "/itens/1/imagem/download")
        String fotoUrl,

        @Schema(description = "ID do produto no catálogo. Nulo para produto ainda não cadastrado.",
                example = "1")
        Integer itemId,

        @Schema(description = "Quantidade encomendada", example = "2")
        Integer quantidade,

        @Schema(description = "Número do orçamento de origem", example = "5")
        Integer orcamentoId,

        @Schema(description = "Nome do cliente do orçamento", example = "João da Silva")
        String clienteNome,

        @Schema(description = "Etapa atual", example = "PENDENTE",
                allowableValues = {"PENDENTE", "RECEBIDA", "CONCLUIDA"})
        StatusEncomenda status,

        @Schema(description = "Data de criação, no aceite do orçamento")
        LocalDateTime criadoEm,

        @Schema(description = "Quando o produto chegou do fornecedor")
        LocalDateTime recebidaEm,

        @Schema(description = "Quando foi entregue ao cliente")
        LocalDateTime concluidaEm
) {}
