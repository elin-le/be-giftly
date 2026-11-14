package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateProductRequest(
    UUID categoryId,

    String name,

    String description,

    BigDecimal price,

    Integer stockQuantity,

    ProductType productType,

    ProductStatus status,

    List<ImageInfo> images
) {}
