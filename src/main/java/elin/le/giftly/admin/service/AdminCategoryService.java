package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.CreateCategoryRequest;
import elin.le.giftly.admin.dto.ImageInfo;
import elin.le.giftly.admin.dto.UpdateCategoryRequest;
import elin.le.giftly.cloudinary.service.CloudinaryService;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.Category;
import elin.le.giftly.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }

    @Transactional
    public Category createCategory(CreateCategoryRequest request) {
        Category category = Category.builder()
            .name(request.name())
            .description(request.description())
            .build();
        applyImage(category, request.image());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(UUID categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        if (request.name() != null) {
            category.setName(request.name());
        }
        if (request.description() != null) {
            category.setDescription(request.description());
        }
        boolean removeImage = Boolean.TRUE.equals(request.removeImage());
        if (request.image() != null && removeImage) {
            throw new BadRequestException("Provide an image or set removeImage, not both");
        }
        if (request.image() != null) {
            String previousPublicId = category.getImagePublicId();
            applyImage(category, request.image());
            if (previousPublicId != null && !previousPublicId.equals(request.image().publicId())) {
                cloudinaryService.deleteImage(previousPublicId);
            }
        } else if (removeImage) {
            if (category.getImagePublicId() != null) {
                cloudinaryService.deleteImage(category.getImagePublicId());
            }
            category.setImagePublicId(null);
            category.setImageSecureUrl(null);
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        if (category.getImagePublicId() != null) {
            cloudinaryService.deleteImage(category.getImagePublicId());
        }
        categoryRepository.delete(category);
    }

    private void applyImage(Category category, ImageInfo image) {
        if (image == null) {
            return;
        }
        if (image.secureUrl() == null || !image.secureUrl().startsWith("https://")) {
            throw new BadRequestException("Category image must include a Cloudinary HTTPS secure URL");
        }
        category.setImagePublicId(image.publicId());
        category.setImageSecureUrl(image.secureUrl());
    }
}
