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
import sound.pezao.backend.security.JwtService;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
}