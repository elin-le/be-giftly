package elin.le.giftly.common.mapper;

import elin.le.giftly.product.dto.ProductResponse;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.product.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

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
        String status = product.getStatus() != null ? product.getStatus().name() : null;
        ProductResponse.CategoryResponse category = product.getCategory() != null
                ? new ProductResponse.CategoryResponse(product.getCategory().getId(), product.getCategory().getName())
                : null;
        List<ProductResponse.ImageResponse> images = product.getImages() != null
                ? toImageResponseList(product.getImages()) : List.of();
        List<ProductResponse.VariantResponse> variantResponses = variants != null
                ? variants.stream().map(v -> new ProductResponse.VariantResponse(
                        v.getId(), v.getSku(), v.getPrice(), v.getStockQuantity(),
                        v.getStatus() != null ? v.getStatus().name() : null
                )).toList() : List.of();
        return new ProductResponse(
                product.getId(), product.getName(), product.getDescription(),
                productType, status, product.getIsCustomBoxEligible(),
                category, images, variantResponses, 0.0, 0L
        );
    }

    default List<ProductResponse> toResponseListWithDetails(List<Product> products) {
        return products.stream().map(p -> toResponseWithDetails(p, p.getVariants())).toList();
    }

    ProductResponse.ImageResponse toImageResponse(ProductImage image);

    List<ProductResponse.ImageResponse> toImageResponseList(List<ProductImage> images);
}
