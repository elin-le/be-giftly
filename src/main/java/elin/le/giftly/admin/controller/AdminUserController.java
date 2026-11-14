package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.AssignRoleRequest;
import elin.le.giftly.admin.dto.UpdateUserStatusRequest;
import elin.le.giftly.admin.service.AdminUserService;
import elin.le.giftly.auth.entity.UserRole;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.common.enums.UserStatus;
import elin.le.giftly.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<User> users = adminUserService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable UUID userId) {
        User user = adminUserService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{userId}/roles")
    public ResponseEntity<ApiResponse<List<UserRole>>> getUserRoles(@PathVariable UUID userId) {
        List<UserRole> roles = adminUserService.getUserRoles(userId);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        User user = adminUserService.updateUserStatus(userId, request.status());
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", user));
    }

    @PostMapping("/roles/assign")
    public ResponseEntity<ApiResponse<UserRole>> assignRole(@Valid @RequestBody AssignRoleRequest request) {
        UserRole userRole = adminUserService.assignRoleToUser(request.userId(), request.roleName());
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully", userRole));
    }

    @DeleteMapping("/roles/remove")
    public ResponseEntity<ApiResponse<Void>> removeRole(
            @RequestParam UUID userId,
            @RequestParam String roleName) {
        adminUserService.removeRoleFromUser(userId, roleName);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully", null));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
