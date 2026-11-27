package elin.le.giftly.product.service;

import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.CustomComponentType;
import elin.le.giftly.common.mapper.ProductMapper;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.dto.PaginatedProductsResponse;
import elin.le.giftly.product.dto.ProductResponse;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.product.repository.ProductVariantRepository;
import elin.le.giftly.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductMapper productMapper;
    private final ReviewRepository reviewRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;

    @Transactional(readOnly = true)
    public PaginatedProductsResponse getProducts(
            UUID categoryId,
            String keyword,
            String type,
            String customComponentType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String cursor,
            int pageSize
    ) {
        validatePriceRange(minPrice, maxPrice);

        int limit = (pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE) + 1;

        UUID cursorUuid = cursor != null ? UUID.fromString(cursor) : null;
        String trimmedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        ProductTypeFilter typeFilter = ProductTypeFilter.from(type);
        ProductType productType = typeFilter.productType();
        String componentType = parseCustomComponentType(customComponentType);
        List<Product> products = productRepository.searchProductsCursor(
                categoryId,
                trimmedKeyword,
                productType != null ? productType.name() : null,
                componentType,
                typeFilter.customBoxEligible(),
                minPrice,
                maxPrice,
                cursorUuid,
                limit
        );

        boolean hasMore = products.size() > limit - 1;
        List<Product> pagedProducts = hasMore ? products.subList(0, limit - 1) : products;

        List<ProductResponse> productResponses = pagedProducts.stream()
                .map(product -> {
                    List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
                    ProductResponse base = productMapper.toResponseWithDetails(product, variants);
                    double avgRating = reviewRepository.findAverageRatingByProductId(product.getId());
                    long reviewCount = reviewRepository.countByProductId(product.getId());
                    return new ProductResponse(
                            base.id(), base.name(), base.description(),
                            base.productType(), base.customComponentType(), base.status(), base.isCustomBoxEligible(),
                            base.category(), base.images(), base.variants(),
                            Math.round(avgRating * 100.0) / 100.0, reviewCount
                    );
                })
                .toList();

        String nextCursor = null;
        if (hasMore && !pagedProducts.isEmpty()) {
            nextCursor = pagedProducts.get(pagedProducts.size() - 1).getId().toString();
        }

        return new PaginatedProductsResponse(productResponses, nextCursor, hasMore, pagedProducts.size());
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if ((minPrice != null && minPrice.signum() < 0) || (maxPrice != null && maxPrice.signum() < 0)) {
            throw new BadRequestException("Price filters must be non-negative");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("minPrice must be less than or equal to maxPrice");
        }
    }

    private String parseCustomComponentType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
        try {
            return CustomComponentType.valueOf(normalized).name();
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unsupported custom component type filter: " + value);
        }
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        ProductResponse base = productMapper.toResponseWithDetails(product, variants);
        double avgRating = reviewRepository.findAverageRatingByProductId(productId);
        long reviewCount = reviewRepository.countByProductId(productId);
        return new ProductResponse(
                base.id(), base.name(), base.description(),
                base.productType(), base.customComponentType(), base.status(), base.isCustomBoxEligible(),
                base.category(), base.images(), base.variants(),
                Math.round(avgRating * 100.0) / 100.0, reviewCount
        );
    }
}
