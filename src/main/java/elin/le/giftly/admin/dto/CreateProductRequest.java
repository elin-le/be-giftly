package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateProductRequest(
    @NotNull(message = "Category ID is required")
    UUID categoryId,

    @NotBlank(message = "Product name is required")
    String name,

    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    BigDecimal price,

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    Integer stockQuantity,

    @NotNull(message = "Product type is required")
    ProductType productType,

    @NotNull(message = "Product status is required")
    ProductStatus status,

    List<String> imageUrls
) {}
