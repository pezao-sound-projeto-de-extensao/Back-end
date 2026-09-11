package sound.pezao.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import sound.pezao.backend.security.JwtService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    // mesmo tipo de mapper que a aplicação injeta: o Boot 4 usa Jackson 3
    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder().build();

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;
    private String validToken;

    @BeforeEach
    void setUp() {
        userDetails = new User(
                "teste@email.com",
                "123",
                List.of(new SimpleGrantedAuthority("GERENCIAR_USUARIOS"))
        );
        validToken = "valid.jwt.token";
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Extração de token via cookie")
    class CookieTokenExtractionTest {

        @Test
        @DisplayName("Deve extrair token do cookie access_token")
        void deveExtrairTokenDoCookieAccessToken() throws ServletException, IOException {
            Cookie accessTokenCookie = new Cookie("access_token", validToken);
            when(request.getCookies()).thenReturn(new Cookie[]{accessTokenCookie});
            when(jwtService.extractUsername(validToken)).thenReturn("teste@email.com");
            when(userDetailsService.loadUserByUsername("teste@email.com")).thenReturn(userDetails);
            when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(true);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertEquals("teste@email.com", SecurityContextHolder.getContext().getAuthentication().getName());
        }

        @Test
        @DisplayName("Deve ignorar cookies sem nome access_token")
        void deveIgnorarCookiesSemNomeAccessToken() throws ServletException, IOException {
            Cookie otherCookie = new Cookie("other_cookie", "value");
            when(request.getCookies()).thenReturn(new Cookie[]{otherCookie});
            when(request.getHeader("Authorization")).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("Deve retornar null quando não há cookies")
        void deveRetornarNullQuandoNaoHaCookies() throws ServletException, IOException {
            when(request.getCookies()).thenReturn(null);
            when(request.getHeader("Authorization")).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Nested
    @DisplayName("Fallback para header Authorization Bearer")
    class BearerFallbackTest {

        @Test
        @DisplayName("Deve usar header Authorization quando cookie não presente")
        void deveUsarHeaderAuthorizationQuandoCookieNaoPresente() throws ServletException, IOException {
            when(request.getCookies()).thenReturn(new Cookie[]{});
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtService.extractUsername(validToken)).thenReturn("teste@email.com");
            when(userDetailsService.loadUserByUsername("teste@email.com")).thenReturn(userDetails);
            when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(true);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertEquals("teste@email.com", SecurityContextHolder.getContext().getAuthentication().getName());
        }

        @Test
        @DisplayName("Deve priorizar cookie sobre header Authorization")
        void devePriorizarCookieSobreHeaderAuthorization() throws ServletException, IOException {
            String cookieToken = "cookie.token";

            Cookie accessTokenCookie = new Cookie("access_token", cookieToken);
            when(request.getCookies()).thenReturn(new Cookie[]{accessTokenCookie});
            when(jwtService.extractUsername(cookieToken)).thenReturn("teste@email.com");
            when(userDetailsService.loadUserByUsername("teste@email.com")).thenReturn(userDetails);
            when(jwtService.isTokenValid(cookieToken, userDetails)).thenReturn(true);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertEquals("teste@email.com", SecurityContextHolder.getContext().getAuthentication().getName());
        }
    }

    @Nested
    @DisplayName("Validação de token")
    class TokenValidationTest {

        @Test
        @DisplayName("Não deve autenticar quando token inválido")
        void naoDeveAutenticarQuandoTokenInvalido() throws ServletException, IOException {
            Cookie accessTokenCookie = new Cookie("access_token", validToken);
            when(request.getCookies()).thenReturn(new Cookie[]{accessTokenCookie});
            when(jwtService.extractUsername(validToken)).thenReturn("teste@email.com");
            when(userDetailsService.loadUserByUsername("teste@email.com")).thenReturn(userDetails);
            when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(false);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("Não deve autenticar quando username nulo")
        void naoDeveAutenticarQuandoUsernameNulo() throws ServletException, IOException {
            Cookie accessTokenCookie = new Cookie("access_token", validToken);
            when(request.getCookies()).thenReturn(new Cookie[]{accessTokenCookie});
            when(jwtService.extractUsername(validToken)).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("Não deve sobrescrever autenticação existente")
        void naoDeveSobrescreverAutenticacaoExistente() throws ServletException, IOException {
            UserDetails existingUser = new User(
                    "existing@email.com",
                    "123",
                    List.of(new SimpleGrantedAuthority("GERENCIAR_USUARIOS"))
            );
            UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(
                    existingUser, null, existingUser.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(existingAuth);

            Cookie accessTokenCookie = new Cookie("access_token", validToken);
            when(request.getCookies()).thenReturn(new Cookie[]{accessTokenCookie});
            when(jwtService.extractUsername(validToken)).thenReturn("teste@email.com");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertEquals("existing@email.com", SecurityContextHolder.getContext().getAuthentication().getName());
        }
    }

    @Nested
    @DisplayName("Resposta de erro quando o token não presta")
    class RespostaDeErroTest {

        private final MockHttpServletRequest requestReal = new MockHttpServletRequest();
        private final MockHttpServletResponse responseReal = new MockHttpServletResponse();
        private final MockFilterChain chainReal = new MockFilterChain();

        @Test
        @DisplayName("Deve responder 401 com problem+json quando o token está expirado")
        void deveResponder401QuandoExpirado() throws Exception {
            requestReal.addHeader("Authorization", "Bearer token-expirado");
            when(jwtService.extractUsername("token-expirado"))
                    .thenThrow(new ExpiredJwtException(null, null, "expirado"));

            jwtAuthenticationFilter.doFilterInternal(requestReal, responseReal, chainReal);

            assertEquals(HttpStatus.UNAUTHORIZED.value(), responseReal.getStatus());
            assertTrue(responseReal.getContentType().contains("application/problem+json"));
            assertTrue(responseReal.getContentAsString().contains("refresh token"));
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            // a requisição não pode seguir para o controller
            assertNull(chainReal.getRequest());
        }

        @Test
        @DisplayName("Deve responder 401 quando o token é inválido")
        void deveResponder401QuandoInvalido() throws Exception {
            requestReal.addHeader("Authorization", "Bearer token-quebrado");
            when(jwtService.extractUsername("token-quebrado"))
                    .thenThrow(new MalformedJwtException("token quebrado"));

            jwtAuthenticationFilter.doFilterInternal(requestReal, responseReal, chainReal);

            assertEquals(HttpStatus.UNAUTHORIZED.value(), responseReal.getStatus());
            assertTrue(responseReal.getContentAsString().contains("inválido"));
            assertNull(chainReal.getRequest());
        }
    }
}
