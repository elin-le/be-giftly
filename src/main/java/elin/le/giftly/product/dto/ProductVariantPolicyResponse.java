package elin.le.giftly.product.dto;

import elin.le.giftly.common.enums.ReturnPolicy;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductVariantPolicyResponse(
        UUID id,
        UUID variantId,
        Boolean isPerishable,
        Integer shelfLifeHours,
        ReturnPolicy returnPolicy,
        BigDecimal advancePaymentPercent
) {}
