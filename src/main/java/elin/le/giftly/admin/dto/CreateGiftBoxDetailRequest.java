package elin.le.giftly.admin.dto;

import java.math.BigDecimal;

public record CreateGiftBoxDetailRequest(
    Double innerLength,
    Double innerWidth,
    Double innerHeight,
    BigDecimal maxWeight,
    String dimensionUnit,
    String weightUnit
) {}
