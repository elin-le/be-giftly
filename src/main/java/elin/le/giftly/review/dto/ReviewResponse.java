package elin.le.giftly.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID userId,
        String userEmail,
        ProductInfo product,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
    public record ProductInfo(
            UUID id,
            String name,
            String thumbnailUrl
    ) {}
}
