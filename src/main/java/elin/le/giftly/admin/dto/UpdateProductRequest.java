package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.CustomComponentType;

import java.util.List;
import java.util.UUID;

public record UpdateProductRequest(
    UUID categoryId,

    String name,

    String description,

    ProductType productType,

    ProductStatus status,

    Boolean isCustomBoxEligible,

    List<VariantImageRequest> commonImages,

    List<UpdateVariantRequest> variants,

    CustomComponentType customComponentType
) {
    public UpdateProductRequest(
            UUID categoryId,
            String name,
            String description,
            ProductType productType,
            ProductStatus status,
            Boolean isCustomBoxEligible,
            List<VariantImageRequest> commonImages,
            List<UpdateVariantRequest> variants
    ) {
        this(categoryId, name, description, productType, status, isCustomBoxEligible, commonImages, variants, null);
    }
}
