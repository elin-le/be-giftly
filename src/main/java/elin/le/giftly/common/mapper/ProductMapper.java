package elin.le.giftly.common.mapper;

import elin.le.giftly.product.dto.ProductResponse;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.review.repository.ReviewRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "productType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    ProductResponse toResponse(Product product);

    default ProductResponse toResponseWithDetails(Product product) {
        ProductResponse base = toResponse(product);
        String productType = product.getProductType() != null ? product.getProductType().name() : null;
        String status = product.getStatus() != null ? product.getStatus().name() : null;
        ProductResponse.CategoryResponse category = product.getCategory() != null
                ? new ProductResponse.CategoryResponse(product.getCategory().getId(), product.getCategory().getName())
                : null;
        List<ProductResponse.ImageResponse> images = product.getImages() != null
                ? toImageResponseList(product.getImages()) : List.of();
        return new ProductResponse(
                base.id(), base.name(), base.description(), base.price(),
                base.stockQuantity(), productType, status, category, images, 0.0, 0L
        );
    }

    default List<ProductResponse> toResponseListWithDetails(List<Product> products) {
        return products.stream().map(this::toResponseWithDetails).toList();
    }

    ProductResponse.ImageResponse toImageResponse(ProductImage image);

    List<ProductResponse.ImageResponse> toImageResponseList(List<ProductImage> images);
}
