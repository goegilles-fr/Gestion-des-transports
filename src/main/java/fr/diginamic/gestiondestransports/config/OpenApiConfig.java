package fr.diginamic.gestiondestransports.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Configuration OpenAPI/Swagger pour la documentation interactive de l'API REST.
 * Génère automatiquement la documentation Swagger UI accessible via /swagger-ui.html.
 * Définit les métadonnées de l'API (titre, description, contact, logo).
 * Facilite les tests et l'intégration avec le frontend Angular.
 */
@Configuration
public class OpenApiConfig {
    /**
     * Crée et configure l'instance OpenAPI pour la documentation Swagger.
     * Définit le titre, la description avec logo, et les informations de contact.
     * La liste de serveurs est vide pour permettre l'utilisation avec n'importe quel environnement.
     *
     * @return l'instance OpenAPI configurée pour l'application de covoiturage
     */
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