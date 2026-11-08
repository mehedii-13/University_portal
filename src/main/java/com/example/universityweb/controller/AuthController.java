package com.example.universityweb.controller;

import com.example.universityweb.config.JwtService;
import com.example.universityweb.dto.AuthRequest;
import com.example.universityweb.dto.AuthResponse;
import com.example.universityweb.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("role", user.getRole().name());
            extraClaims.put("fullName", user.getFullName());
            extraClaims.put("email", user.getEmail());

            String token = jwtService.generateToken(extraClaims, user);

            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .id(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
