package com.rj.journal.App.controller;

import com.rj.journal.App.dto.AuthRequest;
import com.rj.journal.App.dto.AuthResponse;
import com.rj.journal.App.entity.User;
import com.rj.journal.App.security.JwtService;
import com.rj.journal.App.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService users;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserService users, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody AuthRequest request) {
        User user = users.register(request.username(), request.password());
        UserDetails details = org.springframework.security.core.userdetails.User
                .withUsername(user.getUserName()).password(user.getPassword()).roles("USER").build();
        return new AuthResponse(jwtService.generateToken(details), user.getUserName());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserDetails details = (UserDetails) authentication.getPrincipal();
        return new AuthResponse(jwtService.generateToken(details), details.getUsername());
    }
}
