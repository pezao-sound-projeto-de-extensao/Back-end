package sound.pezao.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de negócio para JwtService")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(
                jwtService,
                "jwtSecret",
                "supersecretkey1234567890AmandaDanielHerculesIsaakZaqueu"
        );
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 300000L);
    }

    @Nested
    @DisplayName("Geração de Token")
    class GenerateTokenTest {

        @Test
        @DisplayName("Deve gerar token quando autenticação válida")
        void generateTokenDeveGerarTokenQuandoAutenticacaoValida() {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "teste@email.com",
                    null,
                    List.of(
                            new SimpleGrantedAuthority("GERENCIAR_USUARIOS"),
                            new SimpleGrantedAuthority("VER_RELATORIOS")
                    )
            );

            String token = jwtService.generateToken(authentication);

            assertNotNull(token);
            assertFalse(token.isBlank());
        }
    }

    @Nested
    @DisplayName("Extrair o username do token")
    class ExtractUsernameTest {

        @Test
        @DisplayName("Deve extrair username quando token válido")
        void extractUsernameDeveExtrairUsernameQuandoTokenValido() {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "teste@email.com",
                    null,
                    List.of(new SimpleGrantedAuthority("GERENCIAR_USUARIOS"))
            );

            String token = jwtService.generateToken(authentication);

            String username = jwtService.extractUsername(token);

            assertEquals("teste@email.com", username);
        }

        @Test
        @DisplayName("Deve lançar ExpiredJwtException quando token expirado")
        void extractUsernameDeveLancarExpiredJwtExceptionQuandoTokenExpirado() {
            ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", -1000L);

            var authentication = new UsernamePasswordAuthenticationToken(
                    "teste@email.com",
                    null,
                    List.of(new SimpleGrantedAuthority("GERENCIAR_USUARIOS"))
            );

            String token = jwtService.generateToken(authentication);

            assertThrows(ExpiredJwtException.class,
                    () -> jwtService.extractUsername(token));
        }
    }

    @Nested
    @DisplayName("Verifica se o token é válido")
    class IsTokenValidTest {

        @Test
        @DisplayName("Deve retornar true quando token pertence ao usuário")
        void isTokenValidDeveRetornarTrueQuandoTokenPertenceAoUsuario() {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "teste@email.com",
                    null,
                    List.of(new SimpleGrantedAuthority("GERENCIAR_USUARIOS"))
            );

            String token = jwtService.generateToken(authentication);

            User userDetails = new User(
                    "teste@email.com",
                    "123",
                    List.of(new SimpleGrantedAuthority("GERENCIAR_USUARIOS"))
            );

            boolean valido = jwtService.isTokenValid(token, userDetails);

            assertTrue(valido);
        }

        @Test
        @DisplayName("Deve retornar false quando token não pertence ao usuário")
        void isTokenValidDeveRetornarFalseQuandoTokenNaoPertenceAoUsuario() {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "teste@email.com",
                    null,
                    List.of(new SimpleGrantedAuthority("GERENCIAR_USUARIOS"))
            );

            String token = jwtService.generateToken(authentication);

            User userDetails = new User(
                    "outro@email.com",
                    "123",
                    List.of(new SimpleGrantedAuthority("GERENCIAR_USUARIOS"))
            );

            boolean valido = jwtService.isTokenValid(token, userDetails);

            assertFalse(valido);
        }
    }
}