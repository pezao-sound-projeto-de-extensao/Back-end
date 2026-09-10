package sound.pezao.backend.dto.encomendaDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Indicadores dos cards da tela de encomendas")
public record EncomendaKpisResponse(

        @Schema(description = "Encomendas aguardando a chegada do fornecedor", example = "4")
        long pendentes,

        @Schema(description = "Encomendas recebidas e ainda não entregues", example = "2")
        long recebidas,

        @Schema(description = "Encomendas concluídas no mês corrente", example = "7")
        long concluidasNoMes
) {}
