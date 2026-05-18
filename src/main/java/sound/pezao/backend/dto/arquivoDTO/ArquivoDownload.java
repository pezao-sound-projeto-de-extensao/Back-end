package sound.pezao.backend.dto.arquivoDTO;

import org.springframework.core.io.Resource;

public record ArquivoDownload(
        Resource conteudo,
        String nomeArquivo,
        String mimeType
) {
}
