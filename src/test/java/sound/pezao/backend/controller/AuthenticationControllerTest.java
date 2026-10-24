package sound.pezao.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
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
import sound.pezao.backend.dto.authDTO.EsqueciSenhaRequest;
import sound.pezao.backend.dto.authDTO.RedefinirSenhaRequest;
import sound.pezao.backend.dto.authDTO.RefreshTokenRequest;
import sound.pezao.backend.service.AuthenticationService;
import sound.pezao.backend.service.RecuperacaoSenhaService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes das rotas do AuthenticationController")
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private RecuperacaoSenhaService recuperacaoSenhaService;

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
        @DisplayName("Deve retornar 200 e chamar serviço de autenticação quando login realizado com sucesso")
        void loginDeveRetornar200EChamarServicoQuandoLoginRealizadoComSucesso() throws Exception {
            AuthRequest request = new AuthRequest("teste@email.com", "1234567");
            AuthResponse response = new AuthResponse("access-token", "refresh-token", "teste@email.com", null);

            when(authenticationService.authenticate(request)).thenReturn(response);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(authenticationService).authenticate(request);
        }
    }

    @Nested
    @DisplayName("Teste da rota de refresh")
    class RefreshTest {

        @Test
        @DisplayName("Deve retornar 200 e chamar serviço de refresh quando realizado com sucesso")
        void refreshDeveRetornar200EChamarServicoQuandoRefreshRealizadoComSucesso() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
            AuthResponse response = new AuthResponse("new-access-token", "new-refresh-token", "teste@email.com", null);

            when(authenticationService.refreshToken(request.refreshToken())).thenReturn(response);

            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(authenticationService).refreshToken(request.refreshToken());
        }

        @Test
        @DisplayName("Deve usar o cookie refresh_token quando o corpo vem vazio, como o front envia")
        void refreshDeveUsarCookieQuandoCorpoVazio() throws Exception {
            AuthResponse response = new AuthResponse("new-access-token", "new-refresh-token", "teste@email.com", null);

            when(authenticationService.refreshToken("cookie-refresh-token")).thenReturn(response);

            mockMvc.perform(post("/auth/refresh")
                            .cookie(new Cookie("refresh_token", "cookie-refresh-token"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());

            verify(authenticationService).refreshToken("cookie-refresh-token");
        }
    }

    @Nested
    @DisplayName("Teste da rota de logout")
    class LogoutTest {

        @Test
        @DisplayName("Deve retornar 204 e chamar serviço de logout quando realizado com sucesso")
        void logoutDeveRetornar204EChamarServicoQuandoLogoutRealizadoComSucesso() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

            doNothing().when(authenticationService).logout(request.refreshToken());

            mockMvc.perform(post("/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(authenticationService).logout(request.refreshToken());
        }

        @Test
        @DisplayName("Deve revogar o cookie refresh_token quando o corpo vem vazio, como o front envia")
        void logoutDeveUsarCookieQuandoCorpoVazio() throws Exception {
            mockMvc.perform(post("/auth/logout")
                            .cookie(new Cookie("refresh_token", "cookie-refresh-token"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNoContent());

            verify(authenticationService).logout("cookie-refresh-token");
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

    @Nested
    @DisplayName("Teste das rotas de recuperação de senha por e-mail")
    class RecuperacaoSenhaTest {

        @Test
        @DisplayName("Esqueci a senha deve responder 202 e pedir o envio do e-mail")
        void esqueciSenhaDeveRetornar202() throws Exception {
            mockMvc.perform(post("/auth/esqueci-senha")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new EsqueciSenhaRequest("ana@email.com"))))
                    .andExpect(status().isAccepted());

            verify(recuperacaoSenhaService).solicitar("ana@email.com");
        }

        @Test
        @DisplayName("Esqueci a senha deve recusar e-mail inválido")
        void esqueciSenhaDeveRecusarEmailInvalido() throws Exception {
            mockMvc.perform(post("/auth/esqueci-senha")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new EsqueciSenhaRequest("nao-e-email"))))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(recuperacaoSenhaService);
        }

        @Test
        @DisplayName("Redefinir senha deve responder 204")
        void redefinirSenhaDeveRetornar204() throws Exception {
            mockMvc.perform(post("/auth/redefinir-senha")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RedefinirSenhaRequest("7.segredo", "novaSenha"))))
                    .andExpect(status().isNoContent());

            verify(recuperacaoSenhaService).redefinir("7.segredo", "novaSenha");
        }

        @Test
        @DisplayName("Redefinir senha deve recusar senha curta, com a mesma regra da troca de senha")
        void redefinirSenhaDeveRecusarSenhaCurta() throws Exception {
            mockMvc.perform(post("/auth/redefinir-senha")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RedefinirSenhaRequest("7.segredo", "123"))))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(recuperacaoSenhaService);
        }
    }
}