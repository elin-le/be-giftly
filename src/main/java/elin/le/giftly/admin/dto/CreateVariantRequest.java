package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.VariantStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateVariantRequest(
    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    String sku,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
    BigDecimal price,

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    Integer stockQuantity,

    Double length,
    Double width,
    Double height,
    String dimensionUnit,
    Double weight,
    String weightUnit,

    @NotNull(message = "Variant status is required")
    VariantStatus status,

    List<UUID> optionValueIds,

    CreateGiftBoxDetailRequest giftBoxDetail,
    CreateVariantPolicyRequest variantPolicy,

    List<VariantImageRequest> images
) {}
