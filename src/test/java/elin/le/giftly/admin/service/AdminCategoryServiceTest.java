package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.CreateCategoryRequest;
import elin.le.giftly.admin.dto.UpdateCategoryRequest;
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
            when(categoryRepository.existsById(testCategory.getId())).thenReturn(true);

            adminCategoryService.deleteCategory(testCategory.getId());

            verify(categoryRepository).deleteById(testCategory.getId());
        }

        @Test
        @DisplayName("Should throw when category not found")
        void shouldThrowWhenNotFound() {
            UUID categoryId = UUID.randomUUID();
            when(categoryRepository.existsById(categoryId)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> adminCategoryService.deleteCategory(categoryId));
        }
    }
}
