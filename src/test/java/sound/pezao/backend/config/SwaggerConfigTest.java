package sound.pezao.backend.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sound.pezao.backend.security.JwtService;
import sound.pezao.backend.service.*;
import sound.pezao.backend.facade.MovimentacaoFacade;
import sound.pezao.backend.dashboard.DashboardController;
import sound.pezao.backend.dashboard.DashboardService;
import sound.pezao.backend.relatorio.RelatorioController;
import sound.pezao.backend.relatorio.RelatorioService;
import sound.pezao.backend.handler.GlobalExceptionHandler;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SwaggerConfigTest.TestConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "file:src/main/resources/application.properties")
@MockitoBean(types = {AuthenticationService.class, UsuarioService.class, RecuperacaoSenhaService.class,
        AlertaService.class, CargoService.class, CategoriaService.class, ClienteService.class,
        EncomendaService.class, ItemService.class, MovimentacaoFacade.class, OrcamentoService.class,
        PermissaoService.class, UnidadeService.class, DashboardService.class, RelatorioService.class})
class SwaggerConfigTest {

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
    @Import({SwaggerConfig.class, WebConfig.class, SecurityConfig.class, JwtAuthenticationFilter.class,
            DashboardController.class, RelatorioController.class, GlobalExceptionHandler.class})
    @ComponentScan(basePackages = "sound.pezao.backend.controller")
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void swaggerResourcesStayUnderApiPrefix() throws Exception {
        mockMvc.perform(get("/api/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", startsWith("/api/swagger-ui/")));
        mockMvc.perform(get("/api/swagger-ui/index.html"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/swagger-ui/swagger-initializer.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("restoreSwaggerSession")))
                .andExpect(content().string(containsString(
                        "requestInterceptor: window.stockflowSwaggerSession.requestInterceptor")))
                .andExpect(content().string(containsString(
                        "onComplete: window.stockflowSwaggerSession.onComplete")))
                .andExpect(content().string(containsString("/api/v3/api-docs/swagger-config")));
        mockMvc.perform(get("/api/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/api/v3/api-docs"))
                .andExpect(jsonPath("$.withCredentials").value(true));
        mockMvc.perform(get("/api/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.servers[0].url").value("/"))
                .andExpect(jsonPath("$.paths['/api/auth/login']").exists());
    }

        @Test
        void swaggerUsesPublicOriginBehindProxyEvenWithExpiredCookie() throws Exception {
                mockMvc.perform(get("/api/v3/api-docs")
                                                .header("Host", "app:8080")
                                                .header("X-Forwarded-Host", "stockflow.example")
                                                .header("X-Forwarded-Proto", "https")
                                                .cookie(new Cookie("access_token", "expired-token")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.servers[0].url").value("/"));
                mockMvc.perform(get("/api/auth/me"))
                                .andExpect(status().isUnauthorized());
        }
}