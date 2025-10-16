package fr.diginamic.gestiondestransports.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🚗 Covoit Application API")
                        .description("![Logo](https://covoit.goegilles.fr/images/covoit-logo.png)\n\nAPI REST pour la gestion de covoiturage d'entreprise")
                        .contact(new Contact()
                                .name("Frontend")
                                .url("https://covoit.goegilles.fr")))
                .servers(java.util.Collections.emptyList());
    }
}