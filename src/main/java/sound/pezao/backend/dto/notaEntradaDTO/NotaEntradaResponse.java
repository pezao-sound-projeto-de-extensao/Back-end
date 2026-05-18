package sound.pezao.backend.dto.notaEntradaDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados retornados de uma nota de entrada")
public record NotaEntradaResponse(

        @Schema(description = "ID da nota", example = "1")
        Integer id,

        @Schema(description = "Tipo do arquivo", example = "nota_fiscal", allowableValues = {"imagem", "nota_fiscal"})
        String tipo,

        @Schema(description = "Nome original do arquivo enviado", example = "nf-1042.pdf")
        String nomeArquivo,

        @Schema(description = "Tipo MIME do arquivo", example = "application/pdf")
        String mimeType,

        @Schema(description = "Tamanho do arquivo em bytes", example = "204800")
        Integer tamanhoBytes,

        @Schema(description = "Data do upload")
        LocalDateTime criadoEm,

        @Schema(description = "URL para download da nota", example = "/api/movimentacoes/1/notas/1/download")
        String url

) {}
