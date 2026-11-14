package elin.le.giftly.order.dto;

import java.util.List;

public record PaginatedOrdersResponse(
        List<OrderResponse> orders,
        String nextCursor,
        boolean hasMore,
        int totalSize
) {}
