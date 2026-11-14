package elin.le.giftly.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request to create a new product review")
public record CreateReviewRequest(
        @Schema(description = "Order item ID being reviewed", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Order item ID is required")
        UUID orderItemId,

        @Schema(description = "Rating from 1 to 5", minimum = "1", maximum = "5", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,

        @Schema(description = "Review title (optional, max 255 characters)", example = "Great product!", maxLength = 255)
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Schema(description = "Review comment (optional)", example = "Excellent quality and fast delivery.")
        String comment,

        @Schema(description = "Review image URL (optional, used if not uploading file)", example = "https://example.com/review-image.jpg")
        String imageUrl
) {}
