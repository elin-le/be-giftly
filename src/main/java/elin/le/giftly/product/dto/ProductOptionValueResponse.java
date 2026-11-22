package elin.le.giftly.product.dto;

import java.util.List;
import java.util.UUID;

public record ProductOptionValueResponse(
        UUID id,
        String value,
        String colorHex
) {}
