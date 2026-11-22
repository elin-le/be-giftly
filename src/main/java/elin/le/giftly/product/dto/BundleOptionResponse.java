package elin.le.giftly.product.dto;

import elin.le.giftly.common.enums.PricingMode;

import java.math.BigDecimal;
import java.util.UUID;

public record BundleOptionResponse(
        UUID id,
        UUID productVariantId,
        PricingMode pricingMode,
        BigDecimal fixedPrice
) {}
