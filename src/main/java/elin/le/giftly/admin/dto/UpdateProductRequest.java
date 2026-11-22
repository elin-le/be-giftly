package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;

import java.util.List;
import java.util.UUID;

public record UpdateProductRequest(
    UUID categoryId,

    String name,

    String description,

    ProductType productType,

    ProductStatus status,

    Boolean isCustomBoxEligible,

    List<UpdateVariantRequest> variants
) {}
