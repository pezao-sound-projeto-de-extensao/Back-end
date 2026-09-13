package sound.pezao.backend.relatorio.dto;

import sound.pezao.backend.entities.TipoMovimentacao;

import java.time.LocalDate;

public record MovimentacaoHistoricoDTO(
        LocalDate data,
        String itemNome,
        String categoriaNome,
        TipoMovimentacao tipo,
        Integer quantidade,
        Integer estoqueAntes,
        Integer estoqueDepois,
        String observacao
) {
}
