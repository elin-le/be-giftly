package elin.le.giftly.admin.dto;

import java.util.UUID;

public record UpdateCategoryRequest(
    String name,
    String description
) {}
