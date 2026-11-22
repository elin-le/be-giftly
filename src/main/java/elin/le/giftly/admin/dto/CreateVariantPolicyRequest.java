package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.ReturnPolicy;

import java.math.BigDecimal;

public record CreateVariantPolicyRequest(
    Boolean isPerishable,
    Integer shelfLifeHours,
    ReturnPolicy returnPolicy,
    BigDecimal advancePaymentPercent
) {}
