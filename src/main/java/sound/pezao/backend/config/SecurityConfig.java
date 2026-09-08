package sound.pezao.backend.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    public final String [] ENDPOINTS_PUBLICOS = {
            "/api/auth/login", "/api/auth/trocar-senha", "/api/health", "/api/no-health"
    };
    public final String [] ENDPOINTS_AUTENTICADO = {
            "/api/alertas",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/webjars/**",
            "/v3/api-docs/**",
    };
    public final String [] ENDPOINTS_LISTAR_AUTENTICADO = {
            "/api/categorias", //GET
            "/api/unidades", //GET
            "/api/itens/**", //GET
            "/api/movimentacoes/**" //GET
    };
    public final String [] ENDPOINTS_GERENCIAR_USUARIOS = {
            "/api/usuarios/**",
            "/api/auth/resetar-senha"
    };
    public final String [] ENDPOINTS_GERENCIAR_CARGOS = {
            "/api/cargos/**", "/api/permissoes"
    };
    public final String [] ENDPOINTS_CADASTRAR_ITENS = {
            "/api/categorias/**", //POST
            "/api/itens/**" //POST
    };
    public final String [] ENDPOINTS_EDITAR_ITENS = {
            "/api/categorias/**", //PUT
            "/api/itens/**" //PUT
    };
    public final String [] ENDPOINTS_EXCLUIR_ITENS = {
            "/api/categorias/**", //DELETE
            "/api/itens/**" //DELETE
    };
    public final String [] ENDPOINTS_REGISTRAR_ENTRADA_SAIDA = {
            "/api/movimentacoes/**"
    };
    public final String [] ENDPOINTS_VER_RELATORIOS = {
            "/api/relatorios/**"
    };

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ENDPOINTS_PUBLICOS).permitAll()
                        .requestMatchers(ENDPOINTS_AUTENTICADO).authenticated()
                        .requestMatchers(HttpMethod.GET, ENDPOINTS_LISTAR_AUTENTICADO).authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (allowedOrigins.equals("*")) {
            configuration.setAllowedOrigins(List.of("*"));
            configuration.setAllowCredentials(false);
        } else {
            String[] origins = allowedOrigins.split(",");
            configuration.setAllowedOrigins(Arrays.asList(origins));
            configuration.setAllowCredentials(true);
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration configuration){
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}