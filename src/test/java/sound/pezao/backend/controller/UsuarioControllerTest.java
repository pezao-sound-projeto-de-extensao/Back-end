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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sound.pezao.backend.dto.usuarioDTO.UsuarioRequest;
import sound.pezao.backend.dto.usuarioDTO.UsuarioResponse;
import sound.pezao.backend.service.UsuarioService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de negócio para UsuarioController")
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Busca paginada de usuários")
    class ListarPaginadoTest {

        @Test
        @DisplayName("Deve retornar 200 quando listar usuários paginados com sucesso")
        void listarDeveRetornar200QuandoListarUsuariosPaginadosComSucesso() throws Exception {
            UsuarioResponse usuarioResponse = new UsuarioResponse(
                    1,
                    "Usuário Teste",
                    "teste@email.com",
                    true,
                    null,
                    null,
                    null
            );

            Page<UsuarioResponse> page = new PageImpl<>(
                    List.of(usuarioResponse),
                    PageRequest.of(0, 10),
                    1
            );

            when(usuarioService.listar(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/usuarios")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "nome,asc"))
                    .andExpect(status().isOk());

            verify(usuarioService).listar(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Cadstra usuário")
    class CadastrarTest {

        @Test
        @DisplayName("Deve retornar 201 quando cadastrar usuário com sucesso")
        void cadastrarDeveRetornar201QuandoCadastrarUsuarioComSucesso() throws Exception {
            UsuarioRequest request = new UsuarioRequest(
                    "Usuário Teste",
                    "teste@email.com",
                    1
            );

            UsuarioResponse response = new UsuarioResponse(
                    1,
                    "Usuário Teste",
                    "teste@email.com",
                    true,
                    null,
                    null,
                    null
            );

            when(usuarioService.cadastrar(request)).thenReturn(response);

            mockMvc.perform(post("/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(usuarioService).cadastrar(request);
        }
    }

    @Nested
    @DisplayName("Busca usuário pelo id")
    class ListarPorIdTest {

        @Test
        @DisplayName("Deve retornar 200 quando listar usuário por id com sucesso")
        void listarDeveRetornar200QuandoListarUsuarioPorIdComSucesso() throws Exception {
            UsuarioResponse response = new UsuarioResponse(
                    1,
                    "Usuário Teste",
                    "teste@email.com",
                    true,
                    null,
                    null,
                    null
            );

            when(usuarioService.listar(1)).thenReturn(response);

            mockMvc.perform(get("/usuarios/{id}", 1))
                    .andExpect(status().isOk());

            verify(usuarioService).listar(1);
        }
    }

    @Nested
    @DisplayName("Atualiza usuário")
    class AtualizarTest {

        @Test
        @DisplayName("Deve retornar 200 quando atualizar usuário com sucesso")
        void atualizarDeveRetornar200QuandoAtualizarUsuarioComSucesso() throws Exception {
            UsuarioRequest request = new UsuarioRequest(
                    "Usuário Atualizado",
                    "teste@email.com",
                    1
            );

            UsuarioResponse response = new UsuarioResponse(
                    1,
                    "Usuário Atualizado",
                    "teste@email.com",
                    true,
                    null,
                    null,
                    null
            );

            when(usuarioService.atualizar(1, request)).thenReturn(response);

            mockMvc.perform(put("/usuarios/{id}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(usuarioService).atualizar(1, request);
        }
    }

    @Nested
    @DisplayName("Ativa ou desativa usuário")
    class AtivarTest {

        @Test
        @DisplayName("Deve retornar 204 quando ativar ou desativar usuário com sucesso")
        void ativarDeveRetornar204QuandoAtivarOuDesativarUsuarioComSucesso() throws Exception {
            doNothing().when(usuarioService).ativar(1);

            mockMvc.perform(patch("/usuarios/ativo/{id}", 1))
                    .andExpect(status().isNoContent());

            verify(usuarioService).ativar(1);
        }
    }
}