package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.ComponentRole;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request to update a bundle component.
 */
public record UpdateBundleComponentRequest(
    ComponentRole role,
    UUID productId,
    Integer quantity,
    BigDecimal priceAdjustment,
    String description
) {}
