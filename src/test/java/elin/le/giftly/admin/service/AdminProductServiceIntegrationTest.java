package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.CreateGiftBoxDetailRequest;
import elin.le.giftly.admin.dto.CreateProductRequest;
import elin.le.giftly.admin.dto.CreateVariantRequest;
import elin.le.giftly.admin.dto.CreateVariantPolicyRequest;
import elin.le.giftly.admin.dto.VariantImageRequest;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.ReturnPolicy;
import elin.le.giftly.common.enums.VariantStatus;
import elin.le.giftly.product.entity.Category;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.CategoryRepository;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.product.repository.ProductVariantRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AdminProductService Integration Tests - Variant Saving")
class AdminProductServiceIntegrationTest {

    @Autowired
    private AdminProductService adminProductService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private EntityManager entityManager;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        testCategory = Category.builder()
                .name("Test Category")
                .description("Test category for integration tests")
                .build();
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    @DisplayName("Should save product with variants")
    void shouldSaveProductWithVariants() {
        // Given - Create a product request with variants
        CreateVariantRequest variant1 = new CreateVariantRequest(
                "SKU-001",
                new BigDecimal("50000"),
                100,
                10.0,
                10.0,
                10.0,
                "cm",
                0.5,
                "kg",
                VariantStatus.AVAILABLE,
                null,
                null,
                null,
                null
        );

        CreateVariantRequest variant2 = new CreateVariantRequest(
                "SKU-002",
                new BigDecimal("100000"),
                50,
                20.0,
                20.0,
                20.0,
                "cm",
                1.0,
                "kg",
                VariantStatus.AVAILABLE,
                null,
                null,
                null,
                null
        );

        CreateProductRequest request = new CreateProductRequest(
                testCategory.getId(),
                "Test Product with Variants",
                "This product has multiple variants",
                ProductType.REGULAR,
                ProductStatus.AVAILABLE,
                true,
                List.of(variant1, variant2)
        );

        // When - Create the product
        Product savedProduct = adminProductService.createProduct(request);

        // Then - Product should be saved
        assertNotNull(savedProduct.getId(), "Product ID should not be null");

        // Flush and clear to force DB read
        productRepository.flush();
        entityManager.clear();

        // Re-read from DB
        Product productFromDb = productRepository.findById(savedProduct.getId())
                .orElseThrow(() -> new AssertionError("Product should exist in DB"));

        assertEquals("Test Product with Variants", productFromDb.getName());

        // MOST IMPORTANT: Variants should be saved in DB
        List<ProductVariant> variants = productVariantRepository.findByProductId(savedProduct.getId());
        assertNotNull(variants, "Variants list should not be null");
        assertEquals(2, variants.size(), "Should have 2 variants saved in DB");

        // Verify variant details
        var sku001 = variants.stream()
                .filter(v -> "SKU-001".equals(v.getSku()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Variant SKU-001 should exist"));
        assertEquals(0, new BigDecimal("50000").compareTo(sku001.getPrice()));
        assertEquals(100, sku001.getStockQuantity());

        var sku002 = variants.stream()
                .filter(v -> "SKU-002".equals(v.getSku()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Variant SKU-002 should exist"));
        assertEquals(0, new BigDecimal("100000").compareTo(sku002.getPrice()));
        assertEquals(50, sku002.getStockQuantity());
    }

    @Test
    @DisplayName("Should save product with variants including giftBoxDetail, policy, and images")
    void shouldSaveProductWithFullVariants() {
        // Given
        CreateGiftBoxDetailRequest giftBoxDetail = new CreateGiftBoxDetailRequest(
                15.0, 10.0, 8.0, new BigDecimal("2.0"), "cm", "kg"
        );

        CreateVariantPolicyRequest policy = new CreateVariantPolicyRequest(
                true, 24, ReturnPolicy.RETURNABLE, new BigDecimal("50.00")
        );

        VariantImageRequest image = new VariantImageRequest(
                "https://example.com/image.jpg",
                "https://secure.example.com/image.jpg",
                "public-id-123",
                true,
                0
        );

        CreateVariantRequest variant = new CreateVariantRequest(
                "FULL-SKU-001",
                new BigDecimal("150000"),
                30,
                25.0, 25.0, 25.0, "cm", 2.0, "kg",
                VariantStatus.AVAILABLE,
                null,
                giftBoxDetail,
                policy,
                List.of(image)
        );

        CreateProductRequest request = new CreateProductRequest(
                testCategory.getId(),
                "Full Product",
                "Product with full variant details",
                ProductType.REGULAR,
                ProductStatus.AVAILABLE,
                true,
                List.of(variant)
        );

        // When
        Product savedProduct = adminProductService.createProduct(request);

        // Then
        assertNotNull(savedProduct.getId());

        productRepository.flush();
        entityManager.clear();

        Product productFromDb = productRepository.findById(savedProduct.getId())
                .orElseThrow(() -> new AssertionError("Product should exist in DB"));

        List<ProductVariant> variants = productVariantRepository.findByProductId(savedProduct.getId());
        assertEquals(1, variants.size(), "Should have 1 variant");

        ProductVariant variantFromDb = variants.get(0);
        assertEquals("FULL-SKU-001", variantFromDb.getSku());
        assertNotNull(variantFromDb.getGiftBoxVariantDetail(), "GiftBoxVariantDetail should be saved");
        assertNotNull(variantFromDb.getVariantPolicy(), "VariantPolicy should be saved");
        assertEquals(1, variantFromDb.getImages().size(), "Should have 1 image");
    }

    @Test
    @DisplayName("Should save product without variants")
    void shouldSaveProductWithoutVariants() {
        // Given
        CreateProductRequest request = new CreateProductRequest(
                testCategory.getId(),
                "Product without Variants",
                "No variants",
                ProductType.REGULAR,
                ProductStatus.AVAILABLE,
                false,
                null
        );

        // When
        Product savedProduct = adminProductService.createProduct(request);

        // Then
        assertNotNull(savedProduct.getId());

        productRepository.flush();
        entityManager.clear();

        Product productFromDb = productRepository.findById(savedProduct.getId())
                .orElseThrow(() -> new AssertionError("Product should exist in DB"));

        List<ProductVariant> variants = productVariantRepository.findByProductId(savedProduct.getId());
        assertEquals(0, variants.size(), "Should have 0 variants");
    }
}
