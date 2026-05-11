package sound.pezao.backend.relatorio.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record RelatorioResponseDTO(
        RelatorioKpiDTO kpis,
        List<ItemCriticoDTO> itensCriticos,
        List<ItemMaisMovimentadoDTO> maisMovimentados,
        Page<MovimentacaoHistoricoDTO> historico
) {}