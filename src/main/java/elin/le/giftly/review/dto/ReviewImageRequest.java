package elin.le.giftly.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Cloudinary image metadata attached to a review")
public record ReviewImageRequest(
        @Schema(description = "Cloudinary public ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "giftly/reviews/review-photo")
        @NotBlank(message = "Image public ID is required")
        @Size(max = 255, message = "Image public ID must not exceed 255 characters")
        String publicId,

        @Schema(description = "Cloudinary HTTPS image URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://res.cloudinary.com/demo/image/upload/review-photo.jpg")
        @NotBlank(message = "Image secure URL is required")
        @Size(max = 500, message = "Image secure URL must not exceed 500 characters")
        String secureUrl
) {}