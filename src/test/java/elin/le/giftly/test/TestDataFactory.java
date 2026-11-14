package elin.le.giftly.test;

import elin.le.giftly.admin.dto.CreateCategoryRequest;
import elin.le.giftly.admin.dto.CreateProductRequest;
import elin.le.giftly.admin.dto.ImageInfo;
import elin.le.giftly.auth.entity.Role;
import elin.le.giftly.auth.entity.UserRole;
import elin.le.giftly.cart.dto.AddCartItemRequest;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.ReviewStatus;
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
                .price(new BigDecimal("100000"))
                .stockQuantity(100)
                .productType(ProductType.REGULAR)
                .status(ProductStatus.AVAILABLE)
                .build();
        product.setId(UUID.randomUUID());
        return product;
    }

    /**
     * Create a test product with images.
     */
    public static Product createProductWithImage(Category category) {
        Product product = createProduct(category);
        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl("https://test.com/image.jpg")
                .secureUrl("https://test.com/image.jpg")
                .publicId("test-image")
                .isThumbnail(true)
                .build();
        image.setId(UUID.randomUUID());
        product.setImages(List.of(image));
        return product;
    }

    /**
     * Create a gift box product.
     */
    public static Product createGiftBoxProduct(Category category) {
        Product product = Product.builder()
                .category(category)
                .name("Test Gift Box")
                .description("Test gift box")
                .price(new BigDecimal("50000"))
                .stockQuantity(50)
                .productType(ProductType.GIFT_BOX)
                .status(ProductStatus.AVAILABLE)
                .build();
        product.setId(UUID.randomUUID());
        return product;
    }

    /**
     * Create a wrapping product.
     */
    public static Product createWrappingProduct(Category category) {
        Product product = Product.builder()
                .category(category)
                .name("Test Wrapping")
                .description("Test wrapping")
                .price(new BigDecimal("20000"))
                .stockQuantity(50)
                .productType(ProductType.WRAPPING)
                .status(ProductStatus.AVAILABLE)
                .build();
        product.setId(UUID.randomUUID());
        return product;
    }

    /**
     * Create a ribbon product.
     */
    public static Product createRibbonProduct(Category category) {
        Product product = Product.builder()
                .category(category)
                .name("Test Ribbon")
                .description("Test ribbon")
                .price(new BigDecimal("10000"))
                .stockQuantity(50)
                .productType(ProductType.RIBBON)
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
                .price(new BigDecimal("15000"))
                .stockQuantity(50)
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
    public static AddCartItemRequest createAddCartItemRequest(UUID productId) {
        return new AddCartItemRequest(productId, 1);
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
            UUID boxProductId, List<GiftBoxItemRequest> items) {
        return new CreateGiftBoxRequest(boxProductId, null, null, null, "Happy Birthday!", items);
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
                new BigDecimal("150000"),
                50,
                ProductType.REGULAR,
                ProductStatus.AVAILABLE,
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
