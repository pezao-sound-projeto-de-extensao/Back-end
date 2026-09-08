package sound.pezao.backend.dto.movimentacaoDTO;

import io.swagger.v3.oas.annotations.media.Schema;

public record NotaInfo(
        @Schema(description = "URL para download da nota")
        String url,

        @Schema(description = "Nome original do arquivo")
        String nomeArquivo,

        @Schema(description = "Tipo MIME do arquivo")
        String mimeType,

        @Schema(description = "Tamanho do arquivo em bytes")
        Integer tamanhoBytes
) {}
