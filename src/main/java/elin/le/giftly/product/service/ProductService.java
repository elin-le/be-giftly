package elin.le.giftly.product.service;

import elin.le.giftly.product.dto.PaginatedProductsResponse;
import elin.le.giftly.product.dto.ProductResponse;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;

    @Transactional(readOnly = true)
    public PaginatedProductsResponse getProducts(
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword,
            String cursor,
            int pageSize
    ) {
        int limit = (pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE) + 1;
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));

        UUID cursorUuid = cursor != null ? UUID.fromString(cursor) : null;

        List<Product> products;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // Use fuzzy search with pg_trgm for typo-tolerant search
            String trimmedKeyword = keyword.trim();
            products = productRepository.fuzzySearchProductsNative(
                    trimmedKeyword,
                    categoryId,
                    minPrice,
                    maxPrice,
                    cursorUuid,
                    limit
            );
        } else {
            products = productRepository.findProductsCursor(
                    categoryId,
                    minPrice,
                    maxPrice,
                    null,
                    cursorUuid,
                    pageRequest
            );
        }

        boolean hasMore = products.size() > limit - 1;
        List<Product> pagedProducts = hasMore ? products.subList(0, limit - 1) : products;

        List<ProductResponse> productResponses = pagedProducts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        String nextCursor = null;
        if (hasMore && !pagedProducts.isEmpty()) {
            nextCursor = pagedProducts.get(pagedProducts.size() - 1).getId().toString();
        }

        return new PaginatedProductsResponse(
                productResponses,
                nextCursor,
                hasMore,
                pagedProducts.size()
        );
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse.CategoryResponse categoryResponse = new ProductResponse.CategoryResponse(
                product.getCategory().getId(),
                product.getCategory().getName()
        );

        List<ProductResponse.ImageResponse> imageResponses = product.getImages().stream()
                .map(img -> new ProductResponse.ImageResponse(
                        img.getId(),
                        img.getImageUrl(),
                        img.getPublicId(),
                        img.getSecureUrl(),
                        img.getIsThumbnail()
                ))
                .collect(Collectors.toList());

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getProductType().name(),
                product.getStatus().name(),
                categoryResponse,
                imageResponses
        );
    }
}
