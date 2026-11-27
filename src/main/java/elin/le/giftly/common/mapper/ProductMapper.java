package elin.le.giftly.common.mapper;

import elin.le.giftly.product.dto.ProductResponse;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.product.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "productType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isCustomBoxEligible", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    ProductResponse toResponse(Product product);

    default ProductResponse toResponseWithDetails(Product product, List<ProductVariant> variants) {
        String productType = product.getProductType() != null ? product.getProductType().name() : null;
        String customComponentType = product.getCustomComponentType() != null
                ? product.getCustomComponentType().name() : null;
        String status = product.getStatus() != null ? product.getStatus().name() : null;
        ProductResponse.CategoryResponse category = product.getCategory() != null
                ? new ProductResponse.CategoryResponse(product.getCategory().getId(), product.getCategory().getName())
                : null;
        // Common images: use product-level images first, fall back to first variant's images
        List<ProductResponse.ImageResponse> images = product.getCommonImages() != null && !product.getCommonImages().isEmpty()
                ? toImageResponseList(product.getCommonImages())
                : (variants != null && !variants.isEmpty() && variants.get(0).getImages() != null
                    ? toImageResponseList(variants.get(0).getImages())
                    : List.of());
        List<ProductResponse.VariantResponse> variantResponses = variants != null
                ? variants.stream().map(v -> {
                    List<ProductResponse.ImageResponse> variantImages = v.getImages() != null
                            ? toImageResponseList(v.getImages()) : List.of();
                    List<ProductResponse.OptionValueInfo> optionValues = toOptionValueInfoList(v);
                    BigDecimal finalPrice = calculateFinalPrice(v);
                    return new ProductResponse.VariantResponse(
                            v.getId(), v.getSku(), v.getPrice(), finalPrice, v.getStockQuantity(),
                            v.getLength(), v.getWidth(), v.getHeight(), v.getDimensionUnit(),
                            v.getWeight(), v.getWeightUnit(),
                            v.getStatus() != null ? v.getStatus().name() : null,
                            variantImages, optionValues
                    );
                }).toList() : List.of();
        return new ProductResponse(
                product.getId(), product.getName(), product.getDescription(),
                productType, customComponentType, status, product.getIsCustomBoxEligible(),
                category, images, variantResponses, 0.0, 0L
        );
    }

    default List<ProductResponse.OptionValueInfo> toOptionValueInfoList(ProductVariant variant) {
        if (variant.getOptionValues() == null) {
            return List.of();
        }
        return variant.getOptionValues().stream()
                .map(pov -> new ProductResponse.OptionValueInfo(
                        pov.getOptionValue().getId(),
                        pov.getOptionValue().getOption().getName(),
                        pov.getOptionValue().getValue(),
                        pov.getOptionValue().getColorHex()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Calculate the final price of a variant: base price + sum of all option values' price adjustments.
     */
    default BigDecimal calculateFinalPrice(ProductVariant variant) {
        if (variant.getOptionValues() == null || variant.getOptionValues().isEmpty()) {
            return variant.getPrice();
        }
        BigDecimal adjustments = variant.getOptionValues().stream()
                .map(pov -> pov.getOptionValue().getPriceAdjustment())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return variant.getPrice().add(adjustments);
    }

    default List<ProductResponse> toResponseListWithDetails(List<Product> products) {
        return products.stream().map(p -> toResponseWithDetails(p, p.getVariants())).toList();
    }

    ProductResponse.ImageResponse toImageResponse(ProductImage image);

    List<ProductResponse.ImageResponse> toImageResponseList(List<ProductImage> images);
}
