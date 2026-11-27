package elin.le.giftly.admin.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Request to update a product option group's name and/or its values.
 */
public record UpdateProductOptionRequest(
    @NotBlank(message = "Option name is required")
    String name,

    Integer displayOrder,

    /**
     * If provided, replaces all existing option values with this list.
     * Each item's value field is required; colorHex is optional.
     */
    List<CreateProductOptionValueRequest> values
) {}
