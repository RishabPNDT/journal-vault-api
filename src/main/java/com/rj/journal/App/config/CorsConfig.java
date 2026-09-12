package com.rj.journal.App.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

/**
 * Global Cross-Origin Resource Sharing (CORS) configuration.
 * <p>
 * Key Responsibilities:
 * 1. Allowed Origins: Restricts cross-origin requests to a specific domain via application properties.
 * 2. Allowed Methods: Explicitly enables HTTP verbs (GET, POST, PUT, DELETE, OPTIONS).
 * 3. Allowed Headers: Permits Authorization (for JWT) and Content-Type headers from client applications.
 * 4. Route Mapping: Applies these CORS rules universally to all application endpoints ("/**").
 */

@Configuration
public class CorsConfig {
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origin}") String allowedOrigin) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
