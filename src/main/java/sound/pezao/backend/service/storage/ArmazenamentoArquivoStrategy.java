package sound.pezao.backend.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ArmazenamentoArquivoStrategy {

    String salvar(MultipartFile arquivo, String pasta);

    void salvar(byte[] conteudo, String key, String contentType);

    Resource carregar(String key);

    void deletar(String key);
}