package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.dto.arquivoDTO.ArquivoDownload;
import sound.pezao.backend.dto.imagemProdutoDTO.ImagemProdutoResponse;
import sound.pezao.backend.entities.ImagemProduto;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ImagemProdutoRepository;
import sound.pezao.backend.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ImagemProdutoService")
class ImagemProdutoServiceTest {

    @Mock
    private ImagemProdutoRepository repository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ArmazenamentoArquivoService armazenamento;

    @InjectMocks
    private ImagemProdutoService service;

    private final MultipartFile arquivo =
            new MockMultipartFile("arquivo", "foto.jpg", "image/jpeg", "conteudo".getBytes());

    private Item item(Integer id) {
        Item item = new Item();
        item.setId(id);
        item.setNome("Item " + id);
        return item;
    }

    private ImagemProduto imagem(Integer id, Item item) {
        ImagemProduto imagem = new ImagemProduto();
        imagem.setId(id);
        imagem.setItem(item);
        imagem.setNomeArquivo("foto.jpg");
        imagem.setCaminho("imagens-produto/uuid.jpg");
        imagem.setMimeType("image/jpeg");
        return imagem;
    }

    @Test
    @DisplayName("Deve fazer upload de imagem com sucesso")
    void deveFazerUploadComSucesso() {
        Item item = item(1);
        when(itemRepository.findById(1)).thenReturn(Optional.of(item));
        when(armazenamento.salvar(any(), eq("imagens-produto"))).thenReturn("imagens-produto/uuid.jpg");
        when(repository.save(any(ImagemProduto.class))).thenReturn(imagem(10, item));

        ImagemProdutoResponse resposta = service.upload(1, arquivo);

        assertNotNull(resposta);
        assertEquals(10, resposta.id());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao subir imagem para item inexistente")
    void deveLancarExcecaoQuandoItemInexistente() {
        when(itemRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.upload(99, arquivo));
    }

    @Test
    @DisplayName("Deve listar as imagens de um item")
    void deveListarImagensDoItem() {
        when(itemRepository.existsById(1)).thenReturn(true);
        when(repository.findByItem_Id(1)).thenReturn(List.of(imagem(10, item(1))));

        List<ImagemProdutoResponse> resultado = service.listar(1);

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao listar imagens de item inexistente")
    void deveLancarExcecaoAoListarItemInexistente() {
        when(itemRepository.existsById(99)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.listar(99));
    }

    @Test
    @DisplayName("Deve retornar o arquivo da imagem para download")
    void deveBaixarImagem() {
        ImagemProduto imagem = imagem(10, item(1));
        Resource recurso = new ByteArrayResource("conteudo".getBytes());
        when(repository.findById(10)).thenReturn(Optional.of(imagem));
        when(armazenamento.carregar("imagens-produto/uuid.jpg")).thenReturn(recurso);

        ArquivoDownload download = service.baixar(1, 10);

        assertEquals("foto.jpg", download.nomeArquivo());
        assertEquals(recurso, download.conteudo());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando a imagem não pertence ao item")
    void deveLancarExcecaoQuandoImagemNaoPertenceAoItem() {
        when(repository.findById(10)).thenReturn(Optional.of(imagem(10, item(2))));

        assertThrows(EntityNotFoundException.class, () -> service.baixar(1, 10));
    }

    @Test
    @DisplayName("Deve remover a imagem e o arquivo do storage")
    void deveDeletarImagem() {
        ImagemProduto imagem = imagem(10, item(1));
        when(repository.findById(10)).thenReturn(Optional.of(imagem));

        service.deletar(1, 10);

        verify(repository).delete(imagem);
        verify(armazenamento).deletar("imagens-produto/uuid.jpg");
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao deletar imagem inexistente")
    void deveLancarExcecaoAoDeletarImagemInexistente() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deletar(1, 99));
    }
}
