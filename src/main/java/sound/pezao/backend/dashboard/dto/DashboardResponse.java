package sound.pezao.backend.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import sound.pezao.backend.dto.itemDTO.ItemResponse;

import java.util.List;

@Schema(description = "Dados consolidados da tela inicial")
public record DashboardResponse(

        @Schema(description = "Indicadores dos cards")
        DashboardKpisResponse kpis,

        @Schema(description = "Produtos que precisam de atenção, zerados primeiro")
        List<ItemResponse> itensAtencao,

        @Schema(description = "Indica se há produtos zerados ou em estoque baixo, "
                + "para exibir a faixa de aviso no topo", example = "true")
        boolean possuiAlertas
) {}
