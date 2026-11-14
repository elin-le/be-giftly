package elin.le.giftly.user.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.user.dto.UpdateAdminProfileRequest;
import elin.le.giftly.user.dto.UpdateUserRequest;
import elin.le.giftly.user.dto.UserResponse;
import elin.le.giftly.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Current user profile management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get current user profile", description = "Retrieve the authenticated user's profile information")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse userResponse = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", userResponse));
    }

    @PutMapping
    @Operation(summary = "Update user profile", description = "Update the authenticated user's profile information")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "Updated user profile data", required = true)
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse userResponse = userService.updateUser(request);
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", userResponse));
    }

    @PatchMapping
    @Operation(summary = "Update admin profile", description = "Update profile with secret code verification for admin users")
    public ResponseEntity<ApiResponse<UserResponse>> updateAdminProfile(
            @Parameter(description = "Profile update data with secret code", required = true)
            @Valid @RequestBody UpdateAdminProfileRequest request) {
        UserResponse userResponse = userService.updateAdminProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully with secret code", userResponse));
    }
}
