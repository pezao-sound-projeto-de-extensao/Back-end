package sound.pezao.backend.dto.movimentacaoDTO;

import sound.pezao.backend.dto.notaEntradaDTO.NotaEntradaResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MovimentacaoResponse(
        Integer id,
        ItemResumoResponse item,
        String tipo,
        Integer quantidade,
        Integer estoqueAntes,
        Integer estoqueDepois,
        LocalDate data,
        String observacao,
        LocalDateTime criadoEm,
        List<NotaEntradaResponse> notas
) {
}
