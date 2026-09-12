package com.rj.journal.App.config;

import com.rj.journal.App.security.JwtAuthenticationFilter;
import com.rj.journal.App.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main Spring Security configuration class defining authentication and authorization rules.
 * <p>
 * Key Responsibilities:
 * 1. SecurityFilterChain: Configures stateless REST security (disables CSRF, sets session policy to STATELESS).
 * 2. URL Authorization: Exposes public endpoints (/api/auth/**, /health-check, /error) and locks down all others.
 * 3. Filter Management: Injects custom JwtAuthenticationFilter BEFORE the standard UsernamePasswordAuthenticationFilter.
 * 4. Auth Beans: Configures DaoAuthenticationProvider with BCrypt password encoding and exposes the AuthenticationManager bean.
 */

@Configuration
public class SecurityConfig {

    // Configures HTTP security rules, endpoint permissions, and filter order
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                                   DaoAuthenticationProvider authenticationProvider) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/health-check", "/error").permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService users, BCryptPasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
