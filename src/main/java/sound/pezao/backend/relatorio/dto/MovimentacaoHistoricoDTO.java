package sound.pezao.backend.relatorio.dto;

import java.time.LocalDate;

public record MovimentacaoHistoricoDTO(
        LocalDate data,
        String itemNome,
        String categoriaNome,
        String tipo,
        Integer quantidade,
        Integer estoqueAntes,
        Integer estoqueDepois,
        String observacao
) {
}
