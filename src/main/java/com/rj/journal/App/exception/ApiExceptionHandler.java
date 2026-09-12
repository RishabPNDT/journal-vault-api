package com.rj.journal.App.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream().findFirst().map(x -> x.getDefaultMessage()).orElse("Invalid request");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    @ExceptionHandler(EntryNotFoundException.class)
    ResponseEntity<Map<String, String>> entryNotFound(EntryNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }

    // Covers wrong username/password on login (BadCredentialsException) and
    // any other Spring Security authentication failure. Without this, a
    // failed login falls through to Spring Security's default entry point
    // instead of the consistent {"message": "..."} shape used everywhere
    // else - deliberately generic wording so a caller can't tell whether the
    // username exists.
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Map<String, String>> authenticationFailed(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid username or password"));
    }

    // Catch-all so an unexpected error (DB hiccup, NPE, etc.) never returns
    // a raw stack trace or Spring's default error page - logs the real
    // exception server-side, returns a clean generic message to the client.
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> unexpected(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An unexpected error occurred"));
    }
}
