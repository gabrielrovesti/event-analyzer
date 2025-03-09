package com.analyzer.event_analyzer.controller;

import com.analyzer.event_analyzer.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ReactiveUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        return userDetailsService.findByUsername(request.username())  // Cambiato da getUsername() a username()
                .filter(userDetails ->
                        // In una vera applicazione, qui bisognerebbe usare PasswordEncoder
                        userDetails.getPassword().equals(request.password())  // Cambiato da getPassword() a password()
                )
                .map(userDetails -> {
                    String token = jwtService.generateToken(userDetails);
                    return ResponseEntity.ok(new AuthResponse(token));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // Classi per richiesta/risposta
    public record AuthRequest(String username, String password) {}
    public record AuthResponse(String token) {}
}