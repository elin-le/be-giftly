package elin.le.giftly.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductOptionValueResponse(
        UUID id,
        String value,
        String colorHex,
        BigDecimal priceAdjustment
) {}
