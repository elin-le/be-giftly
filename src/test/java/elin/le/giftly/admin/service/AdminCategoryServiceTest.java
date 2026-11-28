package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.CreateCategoryRequest;
import elin.le.giftly.admin.dto.ImageInfo;
import elin.le.giftly.admin.dto.UpdateCategoryRequest;
import elin.le.giftly.cloudinary.service.CloudinaryService;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.Category;
import elin.le.giftly.product.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminCategoryService Unit Tests")
class AdminCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private AdminCategoryService adminCategoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Test Category")
                .description("Test description")
                .build();
    }

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategories {

        @Test
        @DisplayName("Should return all categories")
        void shouldReturnAllCategories() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> page = new PageImpl<>(List.of(testCategory));
            when(categoryRepository.findAll(pageable)).thenReturn(page);

            Page<Category> result = adminCategoryService.getAllCategories(pageable);

            assertEquals(1, result.getTotalElements());
            verify(categoryRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("getCategoryById")
    class GetCategoryById {

        @Test
        @DisplayName("Should return category by ID")
        void shouldReturnCategoryById() {
            when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

            Category result = adminCategoryService.getCategoryById(testCategory.getId());

            assertEquals(testCategory.getId(), result.getId());
        }

        @Test
        @DisplayName("Should throw when category not found")
        void shouldThrowWhenNotFound() {
            UUID categoryId = UUID.randomUUID();
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> adminCategoryService.getCategoryById(categoryId));
        }
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategory {

        @Test
        @DisplayName("Should create category successfully")
        void shouldCreateCategory() {
            CreateCategoryRequest request = new CreateCategoryRequest("New Category", "New description");
            Category saved = Category.builder().id(UUID.randomUUID()).name("New Category").description("New description").build();
            when(categoryRepository.save(any(Category.class))).thenReturn(saved);

            Category result = adminCategoryService.createCategory(request);

            assertEquals("New Category", result.getName());
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        void savesCloudinaryImageMetadata() {
            ImageInfo image = new ImageInfo("giftly/categories/flowers", "https://res.cloudinary.com/demo/flowers.jpg");
            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Category result = adminCategoryService.createCategory(
                    new CreateCategoryRequest("Flowers", "Flower gifts", image));

            assertEquals(image.publicId(), result.getImagePublicId());
            assertEquals(image.secureUrl(), result.getImageSecureUrl());
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {

        @Test
        @DisplayName("Should update category name")
        void shouldUpdateCategoryName() {
            when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

            UpdateCategoryRequest request = new UpdateCategoryRequest("Updated Name", null);
            Category result = adminCategoryService.updateCategory(testCategory.getId(), request);

            assertEquals("Updated Name", result.getName());
            verify(categoryRepository).save(testCategory);
        }

        @Test
        @DisplayName("Should update category description")
        void shouldUpdateCategoryDescription() {
            when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

            UpdateCategoryRequest request = new UpdateCategoryRequest(null, "Updated description");
            Category result = adminCategoryService.updateCategory(testCategory.getId(), request);

            assertEquals("Updated description", result.getDescription());
        }

        @Test
        void nullRemoveImageKeepsExistingImage() {
            testCategory.setImagePublicId("giftly/categories/current");
            testCategory.setImageSecureUrl("https://res.cloudinary.com/demo/current.jpg");
            when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Category updated = adminCategoryService.updateCategory(
                    testCategory.getId(), new UpdateCategoryRequest(null, null, null, null));

            assertEquals("giftly/categories/current", updated.getImagePublicId());
            assertEquals("https://res.cloudinary.com/demo/current.jpg", updated.getImageSecureUrl());
            verify(cloudinaryService, never()).deleteImage(anyString());
        }

        @Test
        void replacingImageDeletesOldCloudinaryAsset() {
            testCategory.setImagePublicId("giftly/categories/old");
            testCategory.setImageSecureUrl("https://res.cloudinary.com/demo/old.jpg");
            when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
            ImageInfo image = new ImageInfo("giftly/categories/new", "https://res.cloudinary.com/demo/new.jpg");

            Category updated = adminCategoryService.updateCategory(
                    testCategory.getId(), new UpdateCategoryRequest(null, null, image, false));

            assertEquals(image.publicId(), updated.getImagePublicId());
            verify(cloudinaryService).deleteImage("giftly/categories/old");
        }

        @Test
        void rejectsInsecureCategoryImageUrl() {
            when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

            assertThrows(elin.le.giftly.exception.model.BadRequestException.class,
                    () -> adminCategoryService.updateCategory(testCategory.getId(),
                            new UpdateCategoryRequest(null, null,
                                    new ImageInfo("giftly/categories/image", "http://example.test/image.jpg"), false)));
            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw when category not found")
        void shouldThrowWhenNotFound() {
            UUID categoryId = UUID.randomUUID();
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> adminCategoryService.updateCategory(categoryId, new UpdateCategoryRequest("Name", null)));
        }
    }

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategory {

        @Test
        @DisplayName("Should delete category")
        void shouldDeleteCategory() {
            when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

            adminCategoryService.deleteCategory(testCategory.getId());

            verify(categoryRepository).delete(testCategory);
        }

        @Test
        @DisplayName("Should throw when category not found")
        void shouldThrowWhenNotFound() {
            UUID categoryId = UUID.randomUUID();
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> adminCategoryService.deleteCategory(categoryId));
        }
    }
}
