package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.AssignRoleRequest;
import elin.le.giftly.admin.dto.UpdateUserStatusRequest;
import elin.le.giftly.admin.service.AdminUserService;
import elin.le.giftly.auth.entity.UserRole;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.common.dto.PageResponse;
import elin.le.giftly.common.enums.UserStatus;
import elin.le.giftly.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin - Users", description = "Admin APIs for user management, role assignment, and status control. Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users with pagination")
    public ResponseEntity<ApiResponse<PageResponse<User>>> getAllUsers(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<User> users = adminUserService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(users)));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    public ResponseEntity<ApiResponse<User>> getUserById(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId) {
        User user = adminUserService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{userId}/roles")
    @Operation(summary = "Get user roles", description = "Retrieve all roles assigned to a specific user")
    public ResponseEntity<ApiResponse<List<UserRole>>> getUserRoles(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId) {
        List<UserRole> roles = adminUserService.getUserRoles(userId);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "Update user status", description = "Update a user's status (e.g., ACTIVE, SUSPENDED, BANNED)")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "New user status", required = true)
            @Valid @RequestBody UpdateUserStatusRequest request) {
        User user = adminUserService.updateUserStatus(userId, request.status());
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", user));
    }

    @PostMapping("/roles/assign")
    @Operation(summary = "Assign role to user", description = "Assign a new role to a user")
    public ResponseEntity<ApiResponse<UserRole>> assignRole(
            @Parameter(description = "User ID and role name to assign", required = true)
            @Valid @RequestBody AssignRoleRequest request) {
        UserRole userRole = adminUserService.assignRoleToUser(request.userId(), request.roleName());
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully", userRole));
    }

    @DeleteMapping("/roles/remove")
    @Operation(summary = "Remove role from user", description = "Remove a role from a user")
    public ResponseEntity<ApiResponse<Void>> removeRole(
            @Parameter(description = "User ID", required = true)
            @RequestParam UUID userId,
            @Parameter(description = "Role name to remove", required = true)
            @RequestParam String roleName) {
        adminUserService.removeRoleFromUser(userId, roleName);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully", null));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete a user account by ID")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID to delete", required = true)
            @PathVariable UUID userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
