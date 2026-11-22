package elin.le.giftly.auth.controller;

import elin.le.giftly.auth.dto.AuthResponse;
import elin.le.giftly.auth.dto.LoginRequest;
import elin.le.giftly.auth.dto.RegisterRequest;
import elin.le.giftly.auth.service.AuthenticationService;
import elin.le.giftly.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication, registration, and logout APIs")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and password, returns JWT access token and sets refresh token in HttpOnly cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Parameter(description = "Login credentials with email and password", required = true)
            @RequestBody LoginRequest request,
            HttpServletResponse response) {
        Map<String, Object> result = authenticationService.login(request);
        AuthResponse authResponse = (AuthResponse) result.get("authResponse");
        String refreshToken = (String) result.get("refreshToken");

        // Set refresh token in HttpOnly cookie (secure=false for local dev, no HTTPS)
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user account with email, password, and profile info")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Parameter(description = "Registration data with email, password, fullName, and phone", required = true)
            @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        Map<String, Object> result = authenticationService.register(request);
        AuthResponse authResponse = (AuthResponse) result.get("authResponse");
        String refreshToken = (String) result.get("refreshToken");

        // Set refresh token in HttpOnly cookie (secure=false for local dev, no HTTPS)
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(ApiResponse.success("Registration successful", authResponse));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Use the refresh token from HttpOnly cookie to get a new access token. Refresh token is reused (no token rotation).")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(HttpServletRequest request, HttpServletResponse response) {
        // Extract refresh token from cookie
        String refreshTokenCookie = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshTokenCookie = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshTokenCookie == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Refresh token not found in cookie"));
        }

        Map<String, Object> result = authenticationService.refreshToken(refreshTokenCookie);
        AuthResponse authResponse = (AuthResponse) result.get("authResponse");
        String refreshToken = (String) result.get("refreshToken");

        // Refresh token is reused (no token rotation) - just refresh the cookie expiry
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout current user and clear the refresh token cookie.")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            SecurityContextHolder.clearContext();
        }

        // Clear the refresh token cookie
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
        return ResponseEntity.ok(ApiResponse.success("Logout successful.", "Logged out"));
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
