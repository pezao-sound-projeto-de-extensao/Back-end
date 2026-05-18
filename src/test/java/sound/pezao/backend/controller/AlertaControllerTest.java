package sound.pezao.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sound.pezao.backend.dto.alertaDTO.AlertaResponse;
import sound.pezao.backend.service.AlertaService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de negócio para AlertaController")
class AlertaControllerTest {

    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private AlertaController alertaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alertaController)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Busca todos os alertas")
    class ListarTodosTest {

        @Test
        @DisplayName("Deve retornar 200 quando listar todos os alertas com sucesso")
        void findAllDeveRetornar200QuandoListarTodosComSucesso() throws Exception {
            AlertaResponse response = new AlertaResponse(
                    1,
                    "Módulo amplificador 400W",
                    "Som Automotivo",
                    "UN",
                    2,
                    5,
                    "estoque_baixo"
            );

            when(alertaService.findAll()).thenReturn(List.of(response));

            mockMvc.perform(get("/alertas")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(alertaService).findAll();
        }
    }

    @Nested
    @DisplayName("Busca alertas por tipo")
    class BuscarPorTipoTest {

        @Test
        @DisplayName("Deve retornar 200 quando buscar alertas por tipo com sucesso")
        void findByTipoDeveRetornar200QuandoBuscarPorTipoComSucesso() throws Exception {
            String tipo = "estoque_baixo";
            AlertaResponse response = new AlertaResponse(
                    1,
                    "Módulo amplificador 400W",
                    "Som Automotivo",
                    "UN",
                    2,
                    5,
                    tipo
            );

            when(alertaService.findByTipo(tipo)).thenReturn(List.of(response));

            mockMvc.perform(get("/alertas/buscar")
                            .param("tipo", tipo)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(alertaService).findByTipo(tipo);
        }

        @Test
        @DisplayName("Deve retornar 400 quando o parâmetro tipo não for informado")
        void findByTipoDeveRetornar400QuandoTipoNaoInformado() throws Exception {
            mockMvc.perform(get("/alertas/buscar")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}