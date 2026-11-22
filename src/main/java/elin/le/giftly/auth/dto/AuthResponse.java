package elin.le.giftly.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response with JWT access token")
public class AuthResponse {
    @Schema(description = "JWT access token for API authentication", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
    @Schema(description = "User email address", example = "user@example.com")
    private String email;
    @Schema(description = "User full name", example = "John Doe")
    private String fullName;
}
