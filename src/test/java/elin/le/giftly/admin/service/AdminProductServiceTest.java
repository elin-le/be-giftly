package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.CreateProductRequest;
import elin.le.giftly.admin.dto.ImageInfo;
import elin.le.giftly.admin.dto.UpdateProductRequest;
import elin.le.giftly.cloudinary.service.CloudinaryService;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.Category;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.product.repository.CategoryRepository;
import elin.le.giftly.product.repository.ProductImageRepository;
import elin.le.giftly.product.repository.ProductRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminProductService Unit Tests")
class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private AdminProductService adminProductService;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder().id(UUID.randomUUID()).name("Test Category").build();
        testProduct = Product.builder()
                .id(UUID.randomUUID())
                .category(testCategory)
                .name("Test Product")
                .description("Test description")
                .price(new BigDecimal("100000"))
                .stockQuantity(100)
                .productType(ProductType.REGULAR)
                .status(ProductStatus.AVAILABLE)
                .build();
    }

    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("Should return all products")
        void shouldReturnAllProducts() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> page = new PageImpl<>(List.of(testProduct));
            when(productRepository.findAll(pageable)).thenReturn(page);

            Page<Product> result = adminProductService.getAllProducts(pageable);

            assertEquals(1, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("Should return product by ID")
        void shouldReturnProductById() {
            when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

            Product result = adminProductService.getProductById(testProduct.getId());
            assertEquals(testProduct.getId(), result.getId());
        }

        @Test
        @DisplayName("Should throw when product not found")
        void shouldThrowWhenNotFound() {
            UUID productId = UUID.randomUUID();
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> adminProductService.getProductById(productId));
        }
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProduct() {
            CreateProductRequest request = new CreateProductRequest(
                    testCategory.getId(), "New Product", "Description",
                    new BigDecimal("150000"), 50, ProductType.REGULAR, ProductStatus.AVAILABLE, null);
            when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> {
                Product p = i.getArgument(0);
                p.setId(UUID.randomUUID());
                return p;
            });

            Product result = adminProductService.createProduct(request);

            assertNotNull(result.getId());
            assertEquals("New Product", result.getName());
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should create product with images")
        void shouldCreateProductWithImages() {
            ImageInfo imageInfo = new ImageInfo("https://test.com/img.jpg", "test-id");
            CreateProductRequest request = new CreateProductRequest(
                    testCategory.getId(), "New Product", "Description",
                    new BigDecimal("150000"), 50, ProductType.REGULAR, ProductStatus.AVAILABLE,
                    List.of(imageInfo));
            when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> {
                Product p = i.getArgument(0);
                p.setId(UUID.randomUUID());
                return p;
            });

            Product result = adminProductService.createProduct(request);

            assertNotNull(result);
            verify(productImageRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Should throw when category not found")
        void shouldThrowWhenCategoryNotFound() {
            UUID categoryId = UUID.randomUUID();
            CreateProductRequest request = new CreateProductRequest(
                    categoryId, "New Product", "Description",
                    new BigDecimal("150000"), 50, ProductType.REGULAR, ProductStatus.AVAILABLE, null);
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> adminProductService.createProduct(request));
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("Should update product fields")
        void shouldUpdateProductFields() {
            UpdateProductRequest request = new UpdateProductRequest(
                    null, "Updated Name", null, new BigDecimal("200000"), 200, null, null, null);
            when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

            Product result = adminProductService.updateProduct(testProduct.getId(), request);

            assertEquals("Updated Name", result.getName());
            assertEquals(new BigDecimal("200000"), result.getPrice());
            assertEquals(200, result.getStockQuantity());
        }

        @Test
        @DisplayName("Should throw when product not found")
        void shouldThrowWhenNotFound() {
            UUID productId = UUID.randomUUID();
            UpdateProductRequest request = new UpdateProductRequest(
                    null, "Name", null, null, null, null, null, null);
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> adminProductService.updateProduct(productId, request));
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("Should delete product")
        void shouldDeleteProduct() {
            when(productRepository.existsById(testProduct.getId())).thenReturn(true);
            when(productImageRepository.findByProductId(testProduct.getId())).thenReturn(List.of());

            adminProductService.deleteProduct(testProduct.getId());

            verify(productRepository).deleteById(testProduct.getId());
        }

        @Test
        @DisplayName("Should delete product with images cleanup")
        void shouldDeleteProductWithImages() {
            ProductImage image = ProductImage.builder().publicId("test-id").build();
            when(productRepository.existsById(testProduct.getId())).thenReturn(true);
            when(productImageRepository.findByProductId(testProduct.getId())).thenReturn(List.of(image));

            adminProductService.deleteProduct(testProduct.getId());

            verify(cloudinaryService).deleteImages(List.of("test-id"));
            verify(productRepository).deleteById(testProduct.getId());
        }

        @Test
        @DisplayName("Should throw when product not found")
        void shouldThrowWhenNotFound() {
            UUID productId = UUID.randomUUID();
            when(productRepository.existsById(productId)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> adminProductService.deleteProduct(productId));
        }
    }
}
