package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.PricingMode;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateBundleOptionRequest(
    @NotNull(message = "Product variant ID is required")
    UUID productVariantId,

    @NotNull(message = "Pricing mode is required")
    PricingMode pricingMode,

    BigDecimal fixedPrice
) {}
