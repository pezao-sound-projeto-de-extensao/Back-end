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
import sound.pezao.backend.dto.notaEntradaDTO.NotaEntradaResponse;
import sound.pezao.backend.service.NotaEntradaService;

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
@DisplayName("Testes para NotaEntradaController")
class NotaEntradaControllerTest {

    @Mock
    private NotaEntradaService service;

    @InjectMocks
    private NotaEntradaController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private NotaEntradaResponse response() {
        return new NotaEntradaResponse(
                1, "nota_fiscal", "nf.pdf", "application/pdf", 200, null,
                "/api/movimentacoes/1/notas/1/download");
    }

    @Test
    @DisplayName("Deve retornar 201 ao fazer upload de nota")
    void deveRetornar201AoFazerUpload() throws Exception {
        when(service.upload(eq(1), any(), eq("nota_fiscal"))).thenReturn(response());

        mockMvc.perform(multipart("/movimentacoes/{movimentacaoId}/notas", 1)
                        .file(new MockMultipartFile("arquivo", "nf.pdf",
                                "application/pdf", "conteudo".getBytes()))
                        .param("tipo", "nota_fiscal"))
                .andExpect(status().isCreated());

        verify(service).upload(eq(1), any(), eq("nota_fiscal"));
    }

    @Test
    @DisplayName("Deve retornar 200 ao listar notas da movimentação")
    void deveRetornar200AoListar() throws Exception {
        when(service.listar(1)).thenReturn(List.of(response()));

        mockMvc.perform(get("/movimentacoes/{movimentacaoId}/notas", 1))
                .andExpect(status().isOk());

        verify(service).listar(1);
    }

    @Test
    @DisplayName("Deve retornar 200 ao baixar uma nota")
    void deveRetornar200AoBaixar() throws Exception {
        when(service.baixar(1, 1)).thenReturn(new ArquivoDownload(
                new ByteArrayResource("conteudo".getBytes()), "nf.pdf", "application/pdf"));

        mockMvc.perform(get("/movimentacoes/{movimentacaoId}/notas/{notaId}/download", 1, 1))
                .andExpect(status().isOk());

        verify(service).baixar(1, 1);
    }

    @Test
    @DisplayName("Deve retornar 204 ao remover uma nota")
    void deveRetornar204AoRemover() throws Exception {
        doNothing().when(service).deletar(1, 1);

        mockMvc.perform(delete("/movimentacoes/{movimentacaoId}/notas/{notaId}", 1, 1))
                .andExpect(status().isNoContent());

        verify(service).deletar(1, 1);
    }
}
