package elin.le.giftly.test;

import elin.le.giftly.admin.dto.CreateCategoryRequest;
import elin.le.giftly.admin.dto.CreateProductRequest;
import elin.le.giftly.auth.entity.Role;
import elin.le.giftly.auth.entity.UserRole;
import elin.le.giftly.cart.dto.AddCartItemRequest;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.UserStatus;
import elin.le.giftly.giftbox.dto.CreateGiftBoxRequest;
import elin.le.giftly.giftbox.dto.GiftBoxItemRequest;
import elin.le.giftly.order.dto.CreateOrderRequest;
import elin.le.giftly.product.entity.Category;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.review.dto.CreateReviewRequest;
import elin.le.giftly.review.entity.Review;
import elin.le.giftly.user.entity.Address;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.dto.UpdateUserRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Factory class for creating test data objects.
 */
public class TestDataFactory {

    private static int userCounter = 1;
    private static int productCounter = 1;
    private static int categoryCounter = 1;

    /**
     * Create a test user with default values.
     */
    public static User createUser(String email) {
        User user = User.builder()
                .email(email != null ? email : "user" + userCounter++ + "@test.com")
                .password("$2a$10$dummyhashedpassword123456789012345678901234567890") // dummy hash
                .fullName("Test User " + userCounter)
                .phone("0123456789")
                .status(UserStatus.ACTIVE)
                .build();
        user.setId(UUID.randomUUID());
        return user;
    }

    /**
     * Create a test user with role.
     */
    public static User createUserWithRole(String email, String roleName) {
        User user = createUser(email);
        Role role = Role.builder().name(roleName).build();
        role.setId(UUID.randomUUID());
        UserRole userRole = UserRole.builder().user(user).role(role).build();
        return user;
    }

    /**
     * Create an admin user.
     */
    public static User createAdminUser() {
        return createUserWithRole("admin@test.com", "ADMIN");
    }

    /**
     * Create a regular user.
     */
    public static User createRegularUser() {
        return createUserWithRole("user@test.com", "USER");
    }

    /**
     * Create a test category.
     */
    public static Category createCategory(String name) {
        Category category = Category.builder()
                .name(name != null ? name : "Category " + categoryCounter++)
                .description("Test category description")
                .build();
        category.setId(UUID.randomUUID());
        return category;
    }

    /**
     * Create a default test category.
     */
    public static Category createDefaultCategory() {
        return createCategory("Test Category");
    }

    /**
     * Create a test product.
     */
    public static Product createProduct(Category category) {
        Product product = Product.builder()
                .category(category)
                .name("Test Product " + productCounter++)
                .description("Test product description")
                .productType(ProductType.REGULAR)
                .status(ProductStatus.AVAILABLE)
                .isCustomBoxEligible(true)
                .build();
        product.setId(UUID.randomUUID());
        return product;
    }

    /**
     * Create a test product with images.
     */
    public static Product createProductWithImage(Category category) {
        // Images now belong to ProductVariant, not Product
        return createProduct(category);
    }

    /**
     * Create a gift box product.
     */
    public static Product createGiftBoxProduct(Category category) {
        Product product = Product.builder()
                .category(category)
                .name("Test Gift Box")
                .description("Test gift box")
                .productType(ProductType.READY_MADE_GIFT_BOX)
                .status(ProductStatus.AVAILABLE)
                .build();
        product.setId(UUID.randomUUID());
        return product;
    }

    /**
     * Create a packaging accessory product.
     */
    public static Product createPackagingAccessoryProduct(Category category) {
        Product product = Product.builder()
                .category(category)
                .name("Test Packaging Accessory")
                .description("Test packaging accessory")
                .productType(ProductType.PACKAGING_ACCESSORY)
                .status(ProductStatus.AVAILABLE)
                .build();
        product.setId(UUID.randomUUID());
        return product;
    }

    /**
     * Create a greeting card product.
     */
    public static Product createGreetingCardProduct(Category category) {
        Product product = Product.builder()
                .category(category)
                .name("Test Card")
                .description("Test card")
                .productType(ProductType.GREETING_CARD)
                .status(ProductStatus.AVAILABLE)
                .build();
        product.setId(UUID.randomUUID());
        return product;
    }

    /**
     * Create a test address.
     */
    public static Address createAddress(UUID userId) {
        Address address = Address.builder()
                .userId(userId)
                .recipientName("Test Recipient")
                .phone("0987654321")
                .addressLine("123 Test Street")
                .ward("Test Ward")
                .district("Test District")
                .city("Test City")
                .build();
        address.setId(UUID.randomUUID());
        return address;
    }

    /**
     * Create a test review request.
     */
    public static CreateReviewRequest createReviewRequest(UUID orderItemId) {
        return new CreateReviewRequest(orderItemId, 5, "Great product!", "Excellent quality", null);
    }

    /**
     * Create a test add cart item request.
     */
    public static AddCartItemRequest createAddCartItemRequest(UUID productVariantId) {
        return new AddCartItemRequest(productVariantId, 1);
    }

    /**
     * Create a test create order request.
     */
    public static CreateOrderRequest createOrderRequest(UUID addressId) {
        return new CreateOrderRequest(addressId);
    }

    /**
     * Create a test create gift box request.
     */
    public static CreateGiftBoxRequest createGiftBoxRequest(
            UUID boxVariantId, List<GiftBoxItemRequest> items) {
        return new CreateGiftBoxRequest(boxVariantId, items);
    }

    /**
     * Create a test update user request.
     */
    public static UpdateUserRequest createUpdateUserRequest() {
        return new UpdateUserRequest("Updated Name", "0999999999");
    }

    /**
     * Create a test create category request.
     */
    public static CreateCategoryRequest createCategoryRequest() {
        return new CreateCategoryRequest("New Category", "New category description");
    }

    /**
     * Create a test create product request.
     */
    public static CreateProductRequest createProductRequest(UUID categoryId) {
        return new CreateProductRequest(
                categoryId,
                "New Product",
                "New product description",
                ProductType.REGULAR,
                ProductStatus.AVAILABLE,
                true,
                null,
                null
        );
    }

    /**
     * Reset counters (for clean test state).
     */
    public static void resetCounters() {
        userCounter = 1;
        productCounter = 1;
        categoryCounter = 1;
    }
}
