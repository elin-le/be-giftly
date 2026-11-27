package elin.le.giftly.giftbox.service;

import elin.le.giftly.common.enums.CustomComponentType;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.VariantStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.giftbox.dto.CreateGiftBoxRequest;
import elin.le.giftly.giftbox.dto.GiftBoxComponentRequest;
import elin.le.giftly.giftbox.dto.GiftBoxItemRequest;
import elin.le.giftly.giftbox.dto.GiftBoxResponse;
import elin.le.giftly.giftbox.entity.GiftBoxConfig;
import elin.le.giftly.giftbox.entity.GiftBoxConfigItem;
import elin.le.giftly.giftbox.repository.GiftBoxConfigItemRepository;
import elin.le.giftly.giftbox.repository.GiftBoxConfigRepository;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.product.repository.ProductVariantRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftBoxServiceComponentTest {

    @Mock
    private GiftBoxConfigRepository giftBoxConfigRepository;
    @Mock
    private GiftBoxConfigItemRepository giftBoxConfigItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private GiftBoxService giftBoxService;

    private final UUID userId = UUID.randomUUID();
    private final UUID boxVariantId = UUID.randomUUID();
    private final UUID ribbonVariantId = UUID.randomUUID();
    private final UUID secondRibbonVariantId = UUID.randomUUID();
    private final UUID contentVariantId = UUID.randomUUID();
    private final UUID configId = UUID.randomUUID();
    private final List<GiftBoxConfigItem> savedItems = new ArrayList<>();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("demo@example.test", "password"));
        when(userRepository.findByEmail("demo@example.test"))
                .thenReturn(Optional.of(User.builder().id(userId).email("demo@example.test").build()));
        lenient().when(giftBoxConfigRepository.save(any(GiftBoxConfig.class))).thenAnswer(invocation -> {
            GiftBoxConfig config = invocation.getArgument(0);
            config.setId(configId);
            return config;
        });
        lenient().when(productVariantRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return Optional.ofNullable(variants().get(id));
        });
        lenient().when(giftBoxConfigItemRepository.saveAll(ArgumentMatchers.<Iterable<GiftBoxConfigItem>>any()))
                .thenAnswer(invocation -> {
                    Iterable<GiftBoxConfigItem> items = invocation.getArgument(0);
                    items.forEach(savedItems::add);
                    return savedItems;
                });
        lenient().when(giftBoxConfigItemRepository.findByConfigId(configId)).thenAnswer(invocation -> savedItems);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsImagesForBoxAndSelectableComponents() {
        ProductVariant box = variant(boxVariantId, ProductType.READY_MADE_GIFT_BOX,
                CustomComponentType.BOX, "Gift box", "BOX-01", "20.00", true);
        ProductVariant ribbon = variant(ribbonVariantId, ProductType.PACKAGING_ACCESSORY,
                CustomComponentType.RIBBON, "Silk ribbon", "RIBBON-01", "5.00", true);
        ProductVariant content = variant(contentVariantId, ProductType.REGULAR,
                null, "Teddy bear", "TEDDY-01", "50.00", false);

        GiftBoxResponse response = giftBoxService.createGiftBox(new CreateGiftBoxRequest(
                boxVariantId,
                List.of(new GiftBoxItemRequest(contentVariantId, 1)),
                List.of(new GiftBoxComponentRequest(ribbonVariantId))));

        assertEquals("BOX-01", response.boxProduct().sku());
        assertEquals("box-image", response.boxProduct().images().get(0).imageUrl());
        assertEquals(1, response.components().size());
        assertEquals("RIBBON", response.components().get(0).componentType());
        assertEquals("ribbon-image", response.components().get(0).images().get(0).imageUrl());
        assertEquals(new BigDecimal("75.00"), response.totalPrice());
        verify(giftBoxConfigItemRepository).saveAll(any());
    }

    @Test
    void rejectsTwoSelectionsForTheSameComponentType() {
        variant(boxVariantId, ProductType.READY_MADE_GIFT_BOX,
                CustomComponentType.BOX, "Gift box", "BOX-01", "20.00", false);
        variant(ribbonVariantId, ProductType.PACKAGING_ACCESSORY,
                CustomComponentType.RIBBON, "Silk ribbon", "RIBBON-01", "5.00", false);
        variant(secondRibbonVariantId, ProductType.PACKAGING_ACCESSORY,
                CustomComponentType.RIBBON, "Satin ribbon", "RIBBON-02", "7.00", false);
        variant(contentVariantId, ProductType.REGULAR,
                null, "Teddy bear", "TEDDY-01", "50.00", false);

        assertThrows(BadRequestException.class, () -> giftBoxService.createGiftBox(new CreateGiftBoxRequest(
                boxVariantId,
                List.of(new GiftBoxItemRequest(contentVariantId, 1)),
                List.of(new GiftBoxComponentRequest(ribbonVariantId),
                        new GiftBoxComponentRequest(secondRibbonVariantId)))));

        verify(giftBoxConfigItemRepository, never()).saveAll(any());
    }

    @Test
    void preventsRemovingTheRequiredBoxThroughTheContentItemsEndpoint() {
        UUID itemId = UUID.randomUUID();
        GiftBoxConfig config = GiftBoxConfig.builder()
                .id(configId)
                .userId(userId)
                .build();
        GiftBoxConfigItem boxItem = GiftBoxConfigItem.builder()
                .id(itemId)
                .config(config)
                .productVariantId(boxVariantId)
                .componentType(CustomComponentType.BOX)
                .quantity(1)
                .build();
        config.getItems().add(boxItem);
        when(giftBoxConfigRepository.findById(configId)).thenReturn(Optional.of(config));
        when(giftBoxConfigItemRepository.findById(itemId)).thenReturn(Optional.of(boxItem));

        assertThrows(BadRequestException.class, () -> giftBoxService.removeGiftBoxItem(configId, itemId));

        verify(giftBoxConfigItemRepository, never()).delete(boxItem);
    }

    private ProductVariant variant(
            UUID variantId,
            ProductType productType,
            CustomComponentType componentType,
            String name,
            String sku,
            String price,
            boolean includeImage
    ) {
        ProductImage image = ProductImage.builder()
                .id(UUID.randomUUID())
                .imageUrl(componentType == CustomComponentType.BOX ? "box-image" : "ribbon-image")
                .secureUrl("https://images.example.test/image.jpg")
                .publicId("test-image")
                .isPrimary(true)
                .displayOrder(0)
                .build();
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name(name)
                .productType(productType)
                .customComponentType(componentType)
                .status(ProductStatus.AVAILABLE)
                .isCustomBoxEligible(productType == ProductType.REGULAR)
                .commonImages(includeImage ? new ArrayList<>(List.of(image)) : new ArrayList<>())
                .build();
        ProductVariant variant = ProductVariant.builder()
                .id(variantId)
                .product(product)
                .sku(sku)
                .price(new BigDecimal(price))
                .stockQuantity(10)
                .status(VariantStatus.AVAILABLE)
                .images(new ArrayList<>())
                .optionValues(new ArrayList<>())
                .build();
        variants().put(variantId, variant);
        return variant;
    }

    private Map<UUID, ProductVariant> variants() {
        return variantStore;
    }

    private final Map<UUID, ProductVariant> variantStore = new java.util.HashMap<>();
}