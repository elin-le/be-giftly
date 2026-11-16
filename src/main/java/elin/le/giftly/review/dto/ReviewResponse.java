package elin.le.giftly.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Product review response")
public record ReviewResponse(
        @Schema(description = "Review ID") UUID id,
        @Schema(description = "User ID who created the review") UUID userId,
        @Schema(description = "User email") String userEmail,
        @Schema(description = "Product information") ProductInfo product,
        @Schema(description = "Product variant ID") UUID productVariantId,
        @Schema(description = "Rating from 1 to 5", example = "5") Integer rating,
        @Schema(description = "Review title") String title,
        @Schema(description = "Review comment") String comment,
        @Schema(description = "Review image URLs") List<String> imageUrls,
        @Schema(description = "Creation timestamp") LocalDateTime createdAt,
        @Schema(description = "Last update timestamp") LocalDateTime updatedAt
) {
    @Schema(description = "Product information in review")
    public record ProductInfo(
            @Schema(description = "Product ID") UUID id,
            @Schema(description = "Product name") String name,
            @Schema(description = "Product thumbnail URL") String thumbnailUrl
    ) {}
}
