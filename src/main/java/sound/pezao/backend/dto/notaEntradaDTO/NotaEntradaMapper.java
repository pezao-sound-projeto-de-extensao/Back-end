package sound.pezao.backend.dto.notaEntradaDTO;

import sound.pezao.backend.entities.NotaEntrada;

import java.util.List;

public class NotaEntradaMapper {

    public static NotaEntradaResponse toResponse(NotaEntrada nota) {
        return new NotaEntradaResponse(
                nota.getId(),
                nota.getTipo(),
                nota.getNomeArquivo(),
                nota.getMimeType(),
                nota.getTamanhoBytes(),
                nota.getCriadoEm(),
                "/api/movimentacoes/" + nota.getMovimentacao().getId() + "/notas/" + nota.getId() + "/download"
        );
    }

    public static List<NotaEntradaResponse> toResponseList(List<NotaEntrada> notas) {
        return notas.stream().map(NotaEntradaMapper::toResponse).toList();
    }
}
