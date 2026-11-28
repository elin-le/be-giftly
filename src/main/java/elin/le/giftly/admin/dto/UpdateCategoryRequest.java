package elin.le.giftly.admin.dto;

import jakarta.validation.Valid;

public record UpdateCategoryRequest(
    String name,
    String description,
    @Valid ImageInfo image,
    Boolean removeImage
) {
    public UpdateCategoryRequest(String name, String description) {
        this(name, description, null, Boolean.FALSE);
    }
}
