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
import sound.pezao.backend.dto.authDTO.AuthRequest;
import sound.pezao.backend.dto.authDTO.AuthResponse;
import sound.pezao.backend.dto.authDTO.AuthTrocarSenhaRequest;
import sound.pezao.backend.service.AuthenticationService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes das rotas do AuthenticationController")
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Teste da rota de login")
    class LoginTest {

        @Test
        @DisplayName("Deve retornar 200 quando login realizado com sucesso")
        void loginDeveRetornar200QuandoLoginRealizadoComSucesso() throws Exception {
            AuthRequest request = new AuthRequest("teste@email.com", "1234567");
            AuthResponse response = new AuthResponse("token","refresh",  "teste@email.com", null);

            when(authenticationService.authenticate(request)).thenReturn(response);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(authenticationService).authenticate(request);
        }
    }

    @Nested
    @DisplayName("Teste de rota para trocar senha")
    class TrocarSenhaTest {

        @Test
        @DisplayName("Deve retornar 204 quando troca de senha realizada com sucesso")
        void trocarSenhaDeveRetornar204QuandoTrocaDeSenhaRealizadaComSucesso() throws Exception {
            AuthTrocarSenhaRequest request =
                    new AuthTrocarSenhaRequest("teste@email.com", "1234567", "45678910");

            doNothing().when(authenticationService).trocarSenha(request);

            mockMvc.perform(post("/auth/trocar-senha")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(authenticationService).trocarSenha(request);
        }
    }

    @Nested
    @DisplayName("teste de rota para resetar senha")
    class ResetarSenhaTest {

        @Test
        @DisplayName("Deve retornar 204 quando reset de senha realizado com sucesso")
        void resetarSenhaDeveRetornar204QuandoResetDeSenhaRealizadoComSucesso() throws Exception {
            doNothing().when(authenticationService).resetarSenha(1);

            mockMvc.perform(put("/auth/resetar-senha/1")
                            .param("id", "1"))
                    .andExpect(status().isNoContent());

            verify(authenticationService).resetarSenha(1);
        }
    }
}