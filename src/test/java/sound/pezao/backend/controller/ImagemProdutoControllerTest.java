package sound.pezao.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sound.pezao.backend.dto.arquivoDTO.ArquivoDownload;
import sound.pezao.backend.dto.imagemProdutoDTO.ImagemProdutoResponse;
import sound.pezao.backend.service.ImagemProdutoService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ImagemProdutoController")
class ImagemProdutoControllerTest {

    @Mock
    private ImagemProdutoService service;

    @InjectMocks
    private ImagemProdutoController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private ImagemProdutoResponse response() {
        return new ImagemProdutoResponse(
                1, "foto.jpg", "image/jpeg", 100, null,
                "/api/itens/1/imagens/1/download");
    }

    @Test
    @DisplayName("Deve retornar 201 ao fazer upload de imagem")
    void deveRetornar201AoFazerUpload() throws Exception {
        when(service.upload(eq(1), any())).thenReturn(response());

        mockMvc.perform(multipart("/itens/{itemId}/imagens", 1)
                        .file(new MockMultipartFile("arquivo", "foto.jpg",
                                "image/jpeg", "conteudo".getBytes())))
                .andExpect(status().isCreated());

        verify(service).upload(eq(1), any());
    }

    @Test
    @DisplayName("Deve retornar 200 ao listar imagens do item")
    void deveRetornar200AoListar() throws Exception {
        when(service.listar(1)).thenReturn(List.of(response()));

        mockMvc.perform(get("/itens/{itemId}/imagens", 1))
                .andExpect(status().isOk());

        verify(service).listar(1);
    }

    @Test
    @DisplayName("Deve retornar 200 ao baixar uma imagem")
    void deveRetornar200AoBaixar() throws Exception {
        when(service.baixar(1, 1)).thenReturn(new ArquivoDownload(
                new ByteArrayResource("conteudo".getBytes()), "foto.jpg", "image/jpeg"));

        mockMvc.perform(get("/itens/{itemId}/imagens/{imagemId}/download", 1, 1))
                .andExpect(status().isOk());

        verify(service).baixar(1, 1);
    }

    @Test
    @DisplayName("Deve retornar 204 ao remover uma imagem")
    void deveRetornar204AoRemover() throws Exception {
        doNothing().when(service).deletar(1, 1);

        mockMvc.perform(delete("/itens/{itemId}/imagens/{imagemId}", 1, 1))
                .andExpect(status().isNoContent());

        verify(service).deletar(1, 1);
    }
}
