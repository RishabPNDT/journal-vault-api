package com.rj.journal.App.security;

import com.rj.journal.App.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService users;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService users) {
        this.jwtService = jwtService;
        this.users = users;
    }


    /**
     * Intercepts incoming HTTP requests to validate JWT authentication.
     * <p>
     * Process Flow:
     * 1. Checks for a valid "Authorization: Bearer <token>" header. If missing, skips filter.
     * 2. Extracts the username from the JWT token claims.
     * 3. Loads user details and validates token expiration & signature.
     * 4. Populates {@link SecurityContextHolder} with an authenticated principal so Spring
     *    Security permits access to protected endpoints downstream.
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 1. Extract Authorization header & skip filter if not a Bearer token
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        // 2. Strip "Bearer " prefix and extract username from token
        try {
            String token = header.substring(7);
            String username = jwtService.extractUsername(token);

            // 3. Process authentication if username exists and request isn't already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 4. Load full user profile & verify token signature/expiry
                var user = users.loadUserByUsername(username);
                if (jwtService.isValid(token, user)) {

                    // 5. Create authentication object with user details & authorities
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. Set user as authenticated in Spring Security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ignored) {
            // Invalid/expired token will result in empty SecurityContext
        }

        // 7. Pass request downstream to next filter in chain
        chain.doFilter(request, response);
    }
}
