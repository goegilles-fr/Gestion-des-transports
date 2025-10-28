package fr.diginamic.gestiondestransports.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuration principale de la sécurité Spring Security de l'application.
 * Configure l'authentification JWT, les autorisations par rôle, et les endpoints publics/protégés.
 * Désactive CSRF (non nécessaire pour API REST stateless avec JWT).
 * Active CORS pour permettre les requêtes cross-origin depuis le frontend Angular.
 * Définit les règles d'accès :
 * - Endpoints publics : /api/auth/**, /swagger-ui/**, fichiers statiques
 * - Endpoints ADMIN : gestion véhicules entreprise, bannissement utilisateurs
 * - Autres endpoints : authentification requise
 * Intègre le filtre JWT pour valider les tokens sur chaque requête.
 */
@EnableWebSecurity
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {


    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource; // NOSONAR
    /**
     * Configure la chaîne de filtres de sécurité HTTP.
     * Définit les règles d'autorisation par endpoint et méthode HTTP.
     * Désactive CSRF car l'API est stateless (JWT).
     * Active CORS avec la configuration personnalisée.
     * Intègre le filtre JWT avant le filtre d'authentification standard.
     *
     * Règles d'accès :
     * - Authentification et réinitialisation mot de passe : accès public
     * - Documentation Swagger : accès public
     * - Gestion véhicules entreprise (POST/PUT/DELETE) : ADMIN uniquement
     * - Gestion utilisateurs (bannir/vérifier/supprimer) : ADMIN uniquement
     * - Liste utilisateurs et filtres : ADMIN uniquement
     * - Tous les autres endpoints : authentification requise
     *
     * @param http l'objet HttpSecurity à configurer
     * @return la chaîne de filtres de sécurité configurée
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)// NOSONAR
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // Add CORS configuration
                .authorizeHttpRequests(authorize -> authorize

                        // API Auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/utilisateurs/passwordreset").permitAll()
                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()

                        // Static HTML and PNG files
                        .requestMatchers(HttpMethod.GET, "/", "/*.html", "/*.png").permitAll()

                        // Admin endpoints
                        .requestMatchers(HttpMethod.POST,   "/api/vehicules-entreprise").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/vehicules-entreprise/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/vehicules-entreprise/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/utilisateurs/*/verifier").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "api/utilisateurs/*/supprimer").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/utilisateurs/*/bannir").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/utilisateurs/non-verifies").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/utilisateurs/by-role").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/utilisateurs").hasRole("ADMIN")
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Fournit le gestionnaire d'authentification de Spring Security.
     * Utilisé dans AuthController pour valider les credentials (email/password).
     * Délègue l'authentification au CustomUserDetailsService et au PasswordEncoder.
     *
     * @param authenticationConfiguration la configuration d'authentification Spring
     * @return le gestionnaire d'authentification configuré
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    /**
     * Fournit l'encodeur de mots de passe BCrypt.
     * Utilisé pour :
     * - Hasher les mots de passe lors de l'inscription
     * - Vérifier les mots de passe lors de l'authentification
     * BCrypt inclut automatiquement le salt et résiste aux attaques par rainbow tables.
     * Force de hachage par défaut : 10 rounds.
     *
     * @return l'encodeur BCrypt pour les mots de passe
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}