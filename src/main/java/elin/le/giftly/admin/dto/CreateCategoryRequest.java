package elin.le.giftly.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
    @NotBlank(message = "Category name is required")
    String name,

    String description,

    @Valid
    ImageInfo image
) {
    public CreateCategoryRequest(String name, String description) {
        this(name, description, null);
    }
}
