package sound.pezao.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.exception.ArquivoInvalidoException;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmazenamentoArquivoServiceTest {

    @TempDir
    Path tempDir;

    private ArmazenamentoArquivoService service;

    @BeforeEach
    void setUp() {
        service = new ArmazenamentoArquivoService(tempDir.toString());
    }

    @Test
    @DisplayName("Deve salvar o arquivo e retornar a chave relativa dentro da subpasta")
    void deveSalvarArquivoERetornarChave() {
        MultipartFile arquivo = new MockMultipartFile(
                "arquivo", "foto.jpg", "image/jpeg", "conteudo".getBytes());

        String caminho = service.salvar(arquivo, "imagens-produto");

        assertTrue(caminho.startsWith("imagens-produto/"));
        assertTrue(caminho.endsWith(".jpg"));
    }

    @Test
    @DisplayName("Deve lançar ArquivoInvalidoException ao salvar arquivo vazio")
    void deveLancarExcecaoAoSalvarArquivoVazio() {
        MultipartFile vazio = new MockMultipartFile(
                "arquivo", "vazio.jpg", "image/jpeg", new byte[0]);

        assertThrows(ArquivoInvalidoException.class,
                () -> service.salvar(vazio, "imagens-produto"));
    }

    @Test
    @DisplayName("Deve carregar o arquivo salvo com o mesmo conteúdo")
    void deveCarregarArquivoSalvo() throws IOException {
        byte[] conteudo = "imagem-binaria".getBytes();
        MultipartFile arquivo = new MockMultipartFile(
                "arquivo", "foto.png", "image/png", conteudo);
        String caminho = service.salvar(arquivo, "imagens-produto");

        Resource recurso = service.carregar(caminho);

        assertTrue(recurso.exists());
        assertArrayEquals(conteudo, recurso.getInputStream().readAllBytes());
    }

    @Test
    @DisplayName("Deve lançar ArquivoInvalidoException ao carregar arquivo inexistente")
    void deveLancarExcecaoAoCarregarArquivoInexistente() {
        assertThrows(ArquivoInvalidoException.class,
                () -> service.carregar("imagens-produto/nao-existe.jpg"));
    }

    @Test
    @DisplayName("Deve bloquear tentativa de path traversal")
    void deveBloquearPathTraversal() {
        assertThrows(ArquivoInvalidoException.class,
                () -> service.carregar("../../etc/passwd"));
    }

    @Test
    @DisplayName("Deve remover o arquivo salvo do storage")
    void deveRemoverArquivo() {
        MultipartFile arquivo = new MockMultipartFile(
                "arquivo", "foto.jpg", "image/jpeg", "x".getBytes());
        String caminho = service.salvar(arquivo, "imagens-produto");

        service.deletar(caminho);

        assertThrows(ArquivoInvalidoException.class, () -> service.carregar(caminho));
    }
}
