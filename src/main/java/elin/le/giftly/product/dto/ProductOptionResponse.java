package elin.le.giftly.product.dto;

import java.util.List;
import java.util.UUID;

public record ProductOptionResponse(
        UUID id,
        String name,
        Integer displayOrder,
        List<ProductOptionValueResponse> values
) {}
