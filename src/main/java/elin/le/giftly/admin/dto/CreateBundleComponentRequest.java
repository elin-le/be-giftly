package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.ComponentRole;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBundleComponentRequest(
    @NotNull(message = "Bundle variant ID is required")
    UUID bundleVariantId,

    @NotNull(message = "Component variant ID is required")
    UUID componentVariantId,

    @NotNull(message = "Component role is required")
    ComponentRole componentRole,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,

    Boolean isRequired
) {}
