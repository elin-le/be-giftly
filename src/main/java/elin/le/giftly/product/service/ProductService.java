package elin.le.giftly.product.service;

import elin.le.giftly.common.mapper.ProductMapper;
import elin.le.giftly.product.dto.PaginatedProductsResponse;
import elin.le.giftly.product.dto.ProductResponse;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.product.repository.ProductVariantRepository;
import elin.le.giftly.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
            String cursor,
            int pageSize
    ) {
        int limit = (pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE) + 1;
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));

        UUID cursorUuid = cursor != null ? UUID.fromString(cursor) : null;

        List<Product> products;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmedKeyword = keyword.trim();
            products = productRepository.fuzzySearchProductsNative(
                    trimmedKeyword, categoryId, cursorUuid, limit
            );
        } else {
            products = productRepository.findProductsCursor(
                    categoryId, null, cursorUuid, limit
            );
        }

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
                            base.productType(), base.status(), base.isCustomBoxEligible(),
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
}
