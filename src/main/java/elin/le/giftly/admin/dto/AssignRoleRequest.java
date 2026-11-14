package elin.le.giftly.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AssignRoleRequest(
    @NotNull(message = "User ID is required")
    UUID userId,

    @NotBlank(message = "Role name is required")
    String roleName
) {}
