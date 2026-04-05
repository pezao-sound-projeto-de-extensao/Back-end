package sound.pezao.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
//@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    public final String [] ENDPOINTS_PUBLICOS = {
      "/api/auth/login"
    };
    public final String [] ENDPOINTS_AUTENTICADO = {
            "/api/usuarios/ultimo-acesso",
            "/api/auth/logout",
            "/api/auth/trocar-senha",
            "/api/alertas",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/webjars/**",
            "/v3/api-docs/**",
            "/h2-console/**",
            "/h2-console/*/**"
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
    SecurityFilterChain filterChain(HttpSecurity httpSecurity){
        return httpSecurity.
                csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(
                        HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(ENDPOINTS_PUBLICOS).permitAll()
                                .requestMatchers(ENDPOINTS_AUTENTICADO).authenticated()
                                .requestMatchers(HttpMethod.GET, ENDPOINTS_LISTAR_AUTENTICADO).authenticated()
                                .requestMatchers(ENDPOINTS_GERENCIAR_USUARIOS).hasRole("GERENCIAR_USUARIOS")
                                .requestMatchers(ENDPOINTS_GERENCIAR_CARGOS).hasRole("GERENCIAR_CARGOS")
                                .requestMatchers(HttpMethod.POST, ENDPOINTS_CADASTRAR_ITENS).hasRole("CADASTRAR_ITENS")
                                .requestMatchers(HttpMethod.PUT, ENDPOINTS_EDITAR_ITENS).hasRole("EDITAR_ITENS")
                                .requestMatchers(HttpMethod.DELETE, ENDPOINTS_EXCLUIR_ITENS).hasRole("EXCLUIR_ITENS")
                                .requestMatchers(ENDPOINTS_REGISTRAR_ENTRADA_SAIDA).hasAnyRole("REGISTRAR_ENTRADA", "REGISTRAR_SAIDA")
                                .requestMatchers(ENDPOINTS_VER_RELATORIOS).hasRole("VER_RELATORIOS")
                                .anyRequest().authenticated()
                        )
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                .build();
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
