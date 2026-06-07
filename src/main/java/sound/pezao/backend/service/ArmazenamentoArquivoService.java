package sound.pezao.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.exception.ArquivoInvalidoException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ArmazenamentoArquivoService {

    private final Path raiz;

    public ArmazenamentoArquivoService(
            @Value("${app.storage.local.diretorio:./uploads}") String diretorio) {
        this.raiz = Paths.get(diretorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(raiz);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível criar o diretório de armazenamento: " + raiz, e);
        }
    }

    public String salvar(MultipartFile arquivo, String subpasta) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ArquivoInvalidoException("Arquivo vazio ou não enviado.");
        }

        Path destino = resolverSeguro(subpasta);
        String nomeUnico = gerarNomeUnico(arquivo.getOriginalFilename());
        try {
            Files.createDirectories(destino);
            arquivo.transferTo(destino.resolve(nomeUnico));
        } catch (IOException e) {
            throw new ArquivoInvalidoException("Falha ao salvar o arquivo: " + e.getMessage());
        }
        return subpasta + "/" + nomeUnico;
    }

    public Resource carregar(String caminho) {
        Resource recurso = new FileSystemResource(resolverSeguro(caminho));
        if (!recurso.exists() || !recurso.isReadable()) {
            throw new ArquivoInvalidoException("Arquivo não encontrado no storage: " + caminho);
        }
        return recurso;
    }

    public void deletar(String caminho) {
        try {
            Files.deleteIfExists(resolverSeguro(caminho));
        } catch (IOException e) {
            throw new ArquivoInvalidoException("Falha ao remover o arquivo: " + e.getMessage());
        }
    }

    private Path resolverSeguro(String caminho) {
        Path resolvido = raiz.resolve(caminho).normalize();
        if (!resolvido.startsWith(raiz)) {
            throw new ArquivoInvalidoException("Caminho de arquivo inválido: " + caminho);
        }
        return resolvido;
    }

    private String gerarNomeUnico(String nomeOriginal) {
        String extensao = "";
        if (nomeOriginal != null) {
            int ponto = nomeOriginal.lastIndexOf('.');
            if (ponto >= 0 && ponto < nomeOriginal.length() - 1) {
                String candidata = nomeOriginal.substring(ponto + 1);
                if (candidata.matches("[A-Za-z0-9]{1,10}")) {
                    extensao = "." + candidata.toLowerCase();
                }
            }
        }
        return UUID.randomUUID() + extensao;
    }
}
