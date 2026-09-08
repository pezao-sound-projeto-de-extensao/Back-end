package sound.pezao.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.service.storage.ArmazenamentoArquivoStrategy;

@Service
public class ArmazenamentoArquivoService {

    private final ArmazenamentoArquivoStrategy strategy;

    public ArmazenamentoArquivoService(
            ArmazenamentoArquivoStrategy strategy
    ) {
        this.strategy = strategy;
    }

    public String salvar(MultipartFile arquivo, String pasta) {
        return strategy.salvar(arquivo, pasta);
    }

    public Resource carregar(String key) {
        return strategy.carregar(key);
    }

    public void deletar(String key) {
        strategy.deletar(key);
    }
}