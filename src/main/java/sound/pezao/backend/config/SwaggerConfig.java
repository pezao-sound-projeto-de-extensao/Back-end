package sound.pezao.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.resource.TransformedResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/")))
                .info(new Info()
                        .title("Stockflow API")
                        .description("API do sistema de gestão de estoque Pezão Sound")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Pezão Sound")
                                .email("contato@pezaosound.com")
                        )
                );
    }

        @Bean
        public SwaggerIndexTransformer swaggerIndexTransformer(SwaggerUiConfigProperties properties,
                        SwaggerUiOAuthProperties oauthProperties, SwaggerWelcomeCommon welcome,
                        ObjectMapperProvider objectMapperProvider) throws IOException {
                SwaggerIndexPageTransformer delegate = new SwaggerIndexPageTransformer(
                                properties, oauthProperties, welcome, objectMapperProvider);
                String sessionScript = new ClassPathResource("swagger-session.js")
                                .getContentAsString(StandardCharsets.UTF_8);
                return (request, resource, chain) -> {
                        var transformed = delegate.transform(request, resource, chain);
                        if (!"swagger-initializer.js".equals(resource.getFilename())) {
                                return transformed;
                        }
                        String initializer = transformed.getContentAsString(StandardCharsets.UTF_8);
                        return new TransformedResource(transformed,
                                        (initializer + "\n" + sessionScript).getBytes(StandardCharsets.UTF_8));
                };
        }
}