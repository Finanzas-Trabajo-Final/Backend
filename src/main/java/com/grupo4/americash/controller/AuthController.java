package com.grupo4.americash.controller;

import com.grupo4.americash.dto.AuthRequest;
import com.grupo4.americash.dto.AuthResponse;
import com.grupo4.americash.dto.RegisterRequest;
import com.grupo4.americash.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    @Operation(summary = "User Registration", description = "Registers a new user and returns a JWT token")
    public ResponseEntity<AuthResponse>registerUser(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/sign-in")
    @Operation(summary = "User Login", description = "Authenticates a user and returns a JWT token")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.authenticate(authRequest));
    }

}
