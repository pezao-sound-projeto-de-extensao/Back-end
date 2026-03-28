package sound.pezao.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
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
}