package sound.pezao.backend.dto.imagemProdutoDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados retornados de uma imagem de produto")
public record ImagemProdutoResponse(

        @Schema(description = "ID da imagem", example = "1")
        Integer id,

        @Schema(description = "Nome original do arquivo enviado", example = "amplificador.jpg")
        String nomeArquivo,

        @Schema(description = "Tipo MIME do arquivo", example = "image/jpeg")
        String mimeType,

        @Schema(description = "Tamanho do arquivo em bytes", example = "102400")
        Integer tamanhoBytes,

        @Schema(description = "Data do upload")
        LocalDateTime criadoEm,

        @Schema(description = "URL para download da imagem", example = "/api/itens/1/imagens/1/download")
        String url

) {}
