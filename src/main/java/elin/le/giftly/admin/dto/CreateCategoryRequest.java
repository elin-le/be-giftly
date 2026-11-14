package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCategoryRequest(
    @NotBlank(message = "Category name is required")
    String name,

    String description
) {}
