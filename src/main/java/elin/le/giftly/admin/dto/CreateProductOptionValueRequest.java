package elin.le.giftly.admin.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Request to create a product option value (e.g., "Đỏ", "L") within an option group.
 */
public record CreateProductOptionValueRequest(
    @NotBlank(message = "Value is required")
    String value,

    /**
     * Hex color code for color options, e.g., "#FF0000" for red.
     * Optional, only used for color-type options.
     */
    String colorHex,

    /**
     * Price adjustment added to the variant's base price when this option value is selected.
     * Default: 0 (no price change).
     * Example: "Size L" might have +5000, "Color Red" might have +0.
     * Final variant price = variant.basePrice + sum of all linked option values' priceAdjustment.
     */
    BigDecimal priceAdjustment
) {}
