package com.contentos.modules.identity.controller;

import com.contentos.common.response.ApiResponse;
import com.contentos.modules.identity.dto.AuthResponse;
import com.contentos.modules.identity.dto.LoginRequest;
import com.contentos.modules.identity.dto.RegisterRequest;
import com.contentos.modules.identity.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * WHAT IT DOES:
 * REST endpoints for authentication: registration, login, profile.
 *
 * WHY IT EXISTS:
 * Public API for users to create accounts and authenticate. These endpoints
 * are exempt from JWT validation (public).
 *
 * HOW IT CONNECTS:
 * Called by frontend during login/register flows.
 * Returns JWT tokens used for all subsequent requests.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account and organization")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        log.info("Registration request for email: {}", request.getEmail());
        AuthResponse authResponse = authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login request for email: {}", request.getEmail());
        AuthResponse authResponse = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }
}
