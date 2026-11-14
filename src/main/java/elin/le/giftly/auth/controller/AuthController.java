package elin.le.giftly.auth.controller;

import elin.le.giftly.auth.dto.AuthResponse;
import elin.le.giftly.auth.dto.LoginRequest;
import elin.le.giftly.auth.dto.RegisterRequest;
import elin.le.giftly.auth.service.AuthenticationService;
import elin.le.giftly.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication, registration, and logout APIs")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and password, returns JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Parameter(description = "Login credentials with email and password", required = true)
            @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user account with email, password, and profile info")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Parameter(description = "Registration data with email, password, fullName, and phone", required = true)
            @RequestBody RegisterRequest request) {
        AuthResponse response = authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout current user. Client should discard the JWT token after logout.")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            SecurityContextHolder.clearContext();
        }
        String token = extractJwtFromRequest(request);
        // In a stateless JWT setup, the client should discard the token.
        // For future enhancement, you could add the token to a blacklist/revocation list.
        return ResponseEntity.ok(ApiResponse.success("Logout successful. Please discard your token.", "Logged out"));
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
