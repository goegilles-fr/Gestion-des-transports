package fr.diginamic.gestiondestransports.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
/**
 * Configuration CORS (Cross-Origin Resource Sharing) pour l'application.
 * Permet aux applications frontend (Angular) hébergées sur des domaines différents
 * d'accéder à l'API REST en toute sécurité.
 * Configure les origines autorisées, les méthodes HTTP, les headers, et la gestion des credentials.
 * ATTENTION : Configuration actuelle adaptée au développement, à ajuster pour la production.
 */
@Configuration
public class CorsConfig {
    /**
     * Configure la source de configuration CORS pour l'application.
     * Définit les origines autorisées (localhost:4200 pour Angular dev, et les domaines de production).
     * Autorise toutes les méthodes HTTP (GET, POST, PUT, DELETE, OPTIONS).
     * Autorise tous les headers et les credentials (nécessaire pour les tokens JWT).
     * Cache la réponse preflight pendant 1 heure pour optimiser les performances.
     *
     * @return la source de configuration CORS appliquée à tous les endpoints
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // DEVELOPMENT CONFIGURATION - REPLACE FOR PRODUCTION
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",  // Angular dev server
                "http://localhost:3000",  // Alternative port
                "https://dev.goegilles.fr",
                "https://covoit.goegilles.fr" 
        ));

        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (for JWT tokens in headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
