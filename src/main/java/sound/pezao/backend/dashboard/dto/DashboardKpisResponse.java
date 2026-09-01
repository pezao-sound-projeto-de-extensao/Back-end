package sound.pezao.backend.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Indicadores do estoque exibidos nos cards do dashboard")
public record DashboardKpisResponse(

        @Schema(description = "Total de produtos ativos", example = "42")
        long totalProdutos,

        @Schema(description = "Produtos com estoque acima do mínimo", example = "30")
        long estoqueOk,

        @Schema(description = "Produtos com estoque baixo — no mínimo ou abaixo dele, mas ainda com saldo",
                example = "9")
        long emAlerta,

        @Schema(description = "Produtos com estoque zerado", example = "3")
        long zerados
) {}
