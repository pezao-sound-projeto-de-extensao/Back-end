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
import sound.pezao.backend.dto.categoriaDTO.CategoriaRequest;
import sound.pezao.backend.dto.categoriaDTO.CategoriaResponse;
import sound.pezao.backend.service.CategoriaService;

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
@DisplayName("Testes das rotas do CategoriaController")
class CategoriaControllerTest {

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController).build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Lista todas as categorias")
    class FindAllTest {

        @Test
        @DisplayName("Deve retornar 200 quando listar categorias com sucesso")
        void findAllDeveRetornar200QuandoListarCategoriasComSucesso() throws Exception {
            List<CategoriaResponse> response = List.of(
                    new CategoriaResponse("Som automotivo"),
                    new CategoriaResponse("Acessórios")
            );

            when(categoriaService.findAll()).thenReturn(response);

            mockMvc.perform(get("/categorias"))
                    .andExpect(status().isOk());

            verify(categoriaService).findAll();
        }
    }

    @Nested
    @DisplayName("Cria categoria")
    class CreateTest {

        @Test
        @DisplayName("Deve retornar 201 quando criar categoria com sucesso")
        void createDeveRetornar201QuandoCriarCategoriaComSucesso() throws Exception {
            CategoriaRequest request = new CategoriaRequest("Som automotivo");
            CategoriaResponse response = new CategoriaResponse("Som automotivo");

            when(categoriaService.create(request)).thenReturn(response);

            mockMvc.perform(post("/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(categoriaService).create(request);
        }
    }

    @Nested
    @DisplayName("Atualiza categoria")
    class UpdateTest {

        @Test
        @DisplayName("Deve retornar 200 quando atualizar categoria com sucesso")
        void updateDeveRetornar200QuandoAtualizarCategoriaComSucesso() throws Exception {
            CategoriaRequest request = new CategoriaRequest("Som automotivo atualizado");
            CategoriaResponse response = new CategoriaResponse("Som automotivo atualizado");

            when(categoriaService.update(1, request)).thenReturn(response);

            mockMvc.perform(put("/categorias/{id}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(categoriaService).update(1, request);
        }
    }

    @Nested
    @DisplayName("Remove categoria")
    class DeleteTest {

        @Test
        @DisplayName("Deve retornar 204 quando remover categoria com sucesso")
        void deleteDeveRetornar204QuandoRemoverCategoriaComSucesso() throws Exception {
            doNothing().when(categoriaService).delete(1);

            mockMvc.perform(delete("/categorias/{id}", 1))
                    .andExpect(status().isNoContent());

            verify(categoriaService).delete(1);
        }
    }
}
