package elin.le.giftly.product.dto;

import java.util.List;

public record PaginatedProductsResponse(
        List<ProductResponse> products,
        String nextCursor,
        boolean hasMore,
        int totalSize
) {
}
