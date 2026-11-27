package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.VariantStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateVariantRequest(
    UUID id, // null = add new variant to existing product

    @Size(max = 100, message = "SKU must not exceed 100 characters")
    String sku,

    @DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
    BigDecimal price,

    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    Integer stockQuantity,

    Double length,
    Double width,
    Double height,
    String dimensionUnit,
    Double weight,
    String weightUnit,

    VariantStatus status,

    List<UUID> optionValueIds,

    CreateGiftBoxDetailRequest giftBoxDetail,
    CreateVariantPolicyRequest variantPolicy,

    List<VariantImageRequest> images
) {}
