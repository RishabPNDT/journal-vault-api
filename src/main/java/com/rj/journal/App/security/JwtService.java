package com.rj.journal.App.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility service for generating, parsing, and validating JSON Web Tokens (JWT).
 * <p>
 * Key Capabilities:
 * 1. Token Generation: Creates signed HS256 JWTs using configured secret & expiration time.
 * 2. Token Parsing: Claims payload extraction (e.g., subject/username, expiration date).
 * 3. Token Validation: Ensures the token belongs to the matching user and has not expired.
 */

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMs;

    // Initialize HMAC signing key from application properties and set token TTL
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Generate signed JWT containing username as subject and configured expiration date
    public String generateToken(UserDetails user) {
        Date now = new Date();
        return Jwts.builder().subject(user.getUsername()).issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs)).signWith(key).compact();
    }

    // Extract username (JWT Subject) from token payload
    public String extractUsername(String token) { return claims(token).getSubject(); }

    // Verify token belongs to specified user and token expiration date is still in the future
    public boolean isValid(String token, UserDetails user) {
        return user.getUsername().equals(extractUsername(token)) && claims(token).getExpiration().after(new Date());
    }

    // Parse and verify JWT signature using secret key to extract Claims payload
    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
