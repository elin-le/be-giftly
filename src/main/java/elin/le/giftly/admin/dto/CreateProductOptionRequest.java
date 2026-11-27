package elin.le.giftly.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request to create a product option group (e.g., "Màu sắc", "Kích cỡ") for a product.
 */
public record CreateProductOptionRequest(
    @NotBlank(message = "Option name is required")
    String name,

    Integer displayOrder,

    /**
     * List of option values to create along with this option group.
     * e.g., ["Đỏ", "Xanh", "Vàng"] for color option.
     */
    List<CreateProductOptionValueRequest> values
) {}
