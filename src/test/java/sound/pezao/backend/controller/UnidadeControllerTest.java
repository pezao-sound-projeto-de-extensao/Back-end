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
import sound.pezao.backend.dto.unidadesDTO.UnidadeRequest;
import sound.pezao.backend.dto.unidadesDTO.UnidadeResponse;
import sound.pezao.backend.service.UnidadeService;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes das rotas do UnidadeController")
class UnidadeControllerTest {

    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private UnidadeController unidadeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(unidadeController).build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Lista todas as unidades")
    class FindAllTest {

        @Test
        @DisplayName("Deve retornar 200 quando listar unidades com sucesso")
        void findAllDeveRetornar200QuandoListarUnidadesComSucesso() throws Exception {
            List<UnidadeResponse> response = List.of(
                    new UnidadeResponse("Unidade", "un"),
                    new UnidadeResponse("Quilograma", "kg")
            );

            when(unidadeService.findAll()).thenReturn(response);

            mockMvc.perform(get("/unidades"))
                    .andExpect(status().isOk());

            verify(unidadeService).findAll();
        }
    }

    @Nested
    @DisplayName("Cria unidade")
    class CreateTest {

        @Test
        @DisplayName("Deve retornar 201 quando criar unidade com sucesso")
        void createDeveRetornar201QuandoCriarUnidadeComSucesso() throws Exception {
            UnidadeRequest request = new UnidadeRequest("Unidade", "un");
            UnidadeResponse response = new UnidadeResponse("Unidade", "un");

            when(unidadeService.create(request)).thenReturn(response);

            mockMvc.perform(post("/unidades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(unidadeService).create(request);
        }
    }

    @Nested
    @DisplayName("Atualiza unidade")
    class UpdateTest {

        @Test
        @DisplayName("Deve retornar 200 quando atualizar unidade com sucesso")
        void updateDeveRetornar200QuandoAtualizarUnidadeComSucesso() throws Exception {
            UnidadeRequest request = new UnidadeRequest("Quilograma", "kg");
            UnidadeResponse response = new UnidadeResponse("Quilograma", "kg");

            when(unidadeService.update(1, request)).thenReturn(response);

            mockMvc.perform(put("/unidades/{id}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(unidadeService).update(1, request);
        }
    }

    @Nested
    @DisplayName("Remove unidade")
    class DeleteTest {

        @Test
        @DisplayName("Deve retornar 204 quando remover unidade com sucesso")
        void deleteDeveRetornar204QuandoRemoverUnidadeComSucesso() throws Exception {
            doNothing().when(unidadeService).delete(1);

            mockMvc.perform(delete("/unidades/{id}", 1))
                    .andExpect(status().isNoContent());

            verify(unidadeService).delete(1);
        }
    }
}
