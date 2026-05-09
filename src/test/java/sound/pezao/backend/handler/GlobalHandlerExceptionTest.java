package sound.pezao.backend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityExistsException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import sound.pezao.backend.exception.*;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Testes de negócio para GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @RestController
    @RequestMapping("/test-exception")
    static class TestController {

        @GetMapping("/entity-exists")
        public void entityExists() {
            throw new EntityExistsException("duplicado");
        }

        @GetMapping("/entity-not-found")
        public void entityNotFound() {
            throw new EntityNotFoundException("Usuário", 1);
        }

        @GetMapping("/nome-ja-existe")
        public void nomeJaExiste() {
            throw new EntityNomeJaExisteException("Email", "teste@email.com");
        }

        @GetMapping("/inativa")
        public void inativa() {
            throw new EntityInativaException("Entidade inativa", 2);
        }

        @GetMapping("/login-invalido")
        public void loginInvalido() {
            throw new LoginInvalidoException();
        }

        @GetMapping("/primeiro-acesso")
        public void primeiroAcesso() {
            throw new PrimeiroAcessoException("Altere a senha no primeiro acesso");
        }

        @GetMapping("/estoque-insuficiente")
        public void estoqueInsuficiente() {
            throw new EstoqueInsuficienteException(5, 7);
        }

        @GetMapping("/authentication")
        public void authentication() {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        @GetMapping("/acesso-negado")
        public void acessoNegado() {
            throw new AccessDeniedException("Acesso negado");
        }

        @GetMapping("/token-expirado")
        public void tokenExpirado() {
            String secret = "supersecretkey1234567890AmandaDanielHerculesIsaakZaqueu";

            String token = Jwts.builder()
                    .subject("teste@email.com")
                    .issuedAt(new Date(System.currentTimeMillis() - 10000))
                    .expiration(new Date(System.currentTimeMillis() - 1000))
                    .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .compact();

            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseSignedClaims(token);
        }

        @PostMapping("/validacao")
        public void validacao(@Valid @RequestBody TestRequest request) {
        }
    }

    record TestRequest(@NotBlank(message = "nome é obrigatório") String nome) {
    }

    @Test
    @DisplayName("Deve retornar 409 quando EntityExistsException for lançada")
    void deveRetornar409QuandoEntityExistsExceptionForLancada() throws Exception {
        mockMvc.perform(get("/test-exception/entity-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("E-mail já está em uso"))
                .andExpect(jsonPath("$.TimeStamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 404 quando EntityNotFoundException for lançada")
    void deveRetornar404QuandoEntityNotFoundExceptionForLancada() throws Exception {
        mockMvc.perform(get("/test-exception/entity-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.TimeStamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 409 quando EntityNomeJaExisteException for lançada")
    void deveRetornar409QuandoEntityNomeJaExisteExceptionForLancada() throws Exception {
        mockMvc.perform(get("/test-exception/nome-ja-existe"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.TimeStamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 400 quando EntityInativaException for lançada")
    void deveRetornar400QuandoEntityInativaExceptionForLancada() throws Exception {
        mockMvc.perform(get("/test-exception/inativa"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Entidade inativa com id 2 está inativo(a) e não pode ser alterado(a)"))
                .andExpect(jsonPath("$.TimeStamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 401 quando LoginInvalidoException for lançada")
    void deveRetornar401QuandoLoginInvalidoExceptionForLancada() throws Exception {
        mockMvc.perform(get("/test-exception/login-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.TimeStamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 403 quando PrimeiroAcessoException for lançada")
    void deveRetornar403QuandoPrimeiroAcessoExceptionForLancada() throws Exception {
        mockMvc.perform(get("/test-exception/primeiro-acesso"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Altere a senha no primeiro acesso"))
                .andExpect(jsonPath("$.TimeStamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 400 quando EstoqueInsuficienteException for lançada")
    void deveRetornar400QuandoEstoqueInsuficienteExceptionForLancada() throws Exception {
        mockMvc.perform(get("/test-exception/estoque-insuficiente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Estoque insuficiente. Atual: 5, solicitado: 7"))
                .andExpect(jsonPath("$.TimeStamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 401 quando AuthenticationException for lançada")
    void deveRetornar401QuandoAuthenticationExceptionForLancada() throws Exception {
        mockMvc.perform(get("/test-exception/authentication"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciais inválidas"))
                .andExpect(jsonPath("$.TimeStamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 403 quando AccessDeniedException for lançada")
    void deveRetornar403QuandoAccessDeniedExceptionForLancada() throws Exception {
        mockMvc.perform(get("/test-exception/acesso-negado"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Acesso negado"))
                .andExpect(jsonPath("$.TimeStamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 401 quando ExpiredJwtException for lançada")
    void deveRetornar401QuandoExpiredJwtExceptionForLancada() throws Exception {
        mockMvc.perform(get("/test-exception/token-expirado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.TimeStamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 400 quando MethodArgumentNotValidException for lançada")
    void deveRetornar400QuandoMethodArgumentNotValidExceptionForLancada() throws Exception {
        TestRequest request = new TestRequest("");

        mockMvc.perform(post("/test-exception/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Campos inválidos."))
                .andExpect(jsonPath("$.errors.nome").value("nome é obrigatório"))
                .andExpect(jsonPath("$.TimeStamp").exists());
    }
}