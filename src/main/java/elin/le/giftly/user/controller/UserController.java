package elin.le.giftly.user.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.user.dto.UpdateAdminProfileRequest;
import elin.le.giftly.user.dto.UpdateUserRequest;
import elin.le.giftly.user.dto.UserResponse;
import elin.le.giftly.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse userResponse = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", userResponse));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@Valid @RequestBody UpdateUserRequest request) {
        UserResponse userResponse = userService.updateUser(request);
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", userResponse));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateAdminProfile(
            @Valid @RequestBody UpdateAdminProfileRequest request) {
        UserResponse userResponse = userService.updateAdminProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully with secret code", userResponse));
    }
}
