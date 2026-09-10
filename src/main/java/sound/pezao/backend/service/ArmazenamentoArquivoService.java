package sound.pezao.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sound.pezao.backend.exception.ArquivoInvalidoException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
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
