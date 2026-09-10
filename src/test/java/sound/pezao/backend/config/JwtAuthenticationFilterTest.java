package sound.pezao.backend.config;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Permissao;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.security.JwtService;
import sound.pezao.backend.security.UserAuthenticated;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    // o mesmo tipo de mapper que a aplicação injeta em produção
    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder().build();

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockFilterChain chain = new MockFilterChain();

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private UserDetails usuarioAutenticado() {
        Cargo cargo = new Cargo();
        cargo.setPermissoes(Set.of(new Permissao(1, "GERENCIAR_USUARIOS", "")));

        Usuario usuario = Usuario.builder()
                .email("adm@email.com")
                .senhaHash("hash")
                .ativo(true)
                .cargo(cargo)
                .build();

        return new UserAuthenticated(usuario);
    }

    @Test
    @DisplayName("Deve seguir a cadeia sem autenticar quando não há header Authorization")
    void deveSeguirSemHeader() throws Exception {
        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    @DisplayName("Deve autenticar quando o token é válido")
    void deveAutenticarComTokenValido() throws Exception {
        request.addHeader("Authorization", "Bearer token-valido");
        when(jwtService.extractUsername("token-valido")).thenReturn("adm@email.com");
        when(userDetailsService.loadUserByUsername("adm@email.com")).thenReturn(usuarioAutenticado());
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("adm@email.com",
                SecurityContextHolder.getContext().getAuthentication().getName());
        assertNotNull(chain.getRequest());
    }

    @Test
    @DisplayName("Deve responder 401 com problem+json quando o token está expirado")
    void deveResponder401QuandoExpirado() throws Exception {
        request.addHeader("Authorization", "Bearer token-expirado");
        when(jwtService.extractUsername("token-expirado"))
                .thenThrow(new ExpiredJwtException(null, null, "expirado"));

        filter.doFilter(request, response, chain);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(response.getContentType().contains("application/problem+json"));
        assertTrue(response.getContentAsString().contains("refresh token"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        // a requisição não pode seguir para o controller
        assertNull(chain.getRequest());
    }

    @Test
    @DisplayName("Deve responder 401 quando o token é inválido")
    void deveResponder401QuandoInvalido() throws Exception {
        request.addHeader("Authorization", "Bearer token-quebrado");
        when(jwtService.extractUsername("token-quebrado"))
                .thenThrow(new MalformedJwtException("token quebrado"));

        filter.doFilter(request, response, chain);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("inválido"));
        assertNull(chain.getRequest());
        verify(userDetailsService, org.mockito.Mockito.never()).loadUserByUsername(anyString());
    }
}
