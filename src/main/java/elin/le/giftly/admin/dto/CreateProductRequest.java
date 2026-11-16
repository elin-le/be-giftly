package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public record CreateProductRequest(
    @NotNull(message = "Category ID is required")
    UUID categoryId,

    @NotBlank(message = "Product name is required")
    String name,

    String description,

    @NotNull(message = "Product type is required")
    ProductType productType,

    @NotNull(message = "Product status is required")
    ProductStatus status,

    Boolean isCustomBoxEligible,

    List<ImageInfo> images
) {}
