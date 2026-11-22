package elin.le.giftly.product.dto;

import elin.le.giftly.common.enums.ComponentRole;

import java.util.UUID;

public record BundleComponentResponse(
        UUID id,
        UUID componentVariantId,
        ComponentRole componentRole,
        Integer quantity,
        Boolean isRequired
) {}
