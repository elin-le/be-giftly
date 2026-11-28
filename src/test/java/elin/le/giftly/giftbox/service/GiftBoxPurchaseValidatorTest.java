package elin.le.giftly.giftbox.service;

import elin.le.giftly.common.enums.CustomComponentType;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.VariantStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.giftbox.entity.GiftBoxConfig;
import elin.le.giftly.giftbox.entity.GiftBoxConfigItem;
import elin.le.giftly.giftbox.repository.GiftBoxConfigItemRepository;
import elin.le.giftly.giftbox.repository.GiftBoxConfigRepository;
import elin.le.giftly.product.entity.GiftBoxVariantDetail;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GiftBoxPurchaseValidatorTest {

    @Mock
    private GiftBoxConfigRepository giftBoxConfigRepository;
    @Mock
    private GiftBoxConfigItemRepository giftBoxConfigItemRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @InjectMocks
    private GiftBoxPurchaseValidator validator;

    private final UUID userId = UUID.randomUUID();
    private final UUID configId = UUID.randomUUID();
    private final UUID boxId = UUID.randomUUID();
    private final UUID firstContentId = UUID.randomUUID();
    private final UUID secondContentId = UUID.randomUUID();
    private final UUID thirdContentId = UUID.randomUUID();
    private final UUID cardId = UUID.randomUUID();
    private final Map<UUID, ProductVariant> variants = new HashMap<>();
    private GiftBoxConfig config;

    @BeforeEach
    void setUp() {
        config = GiftBoxConfig.builder().id(configId).userId(userId).build();
        variants.clear();
        variants.put(boxId, variant(boxId, ProductType.READY_MADE_GIFT_BOX, CustomComponentType.BOX, "Gift box", "100.00"));
        variants.put(firstContentId, variant(firstContentId, ProductType.REGULAR, null, "Gift item one", "10.00"));
        variants.put(secondContentId, variant(secondContentId, ProductType.REGULAR, null, "Gift item two", "10.00"));
        variants.put(thirdContentId, variant(thirdContentId, ProductType.REGULAR, null, "Gift item three", "10.00"));
        variants.put(cardId, variant(cardId, ProductType.GREETING_CARD, CustomComponentType.GREETING_CARD, "Greeting card", "5.00"));

        lenient().when(giftBoxConfigRepository.findById(configId)).thenReturn(Optional.of(config));
        lenient().when(productVariantRepository.findById(any(UUID.class))).thenAnswer(invocation ->
                Optional.ofNullable(variants.get(invocation.getArgument(0))));
    }

    @Test
    void excludesGreetingCardFromTheThreeRequiredContents() {
        setItems(List.of(
                item(boxId, 1, CustomComponentType.BOX),
                item(firstContentId, 1, null),
                item(secondContentId, 1, null),
                item(cardId, 1, CustomComponentType.GREETING_CARD)));

        assertThrows(BadRequestException.class, () -> validator.validate(configId, userId, 1));
    }

    @Test
    void acceptsThreeContentsAndCalculatesAllChargedComponents() {
        setItems(validItemsWithCard());

        GiftBoxPurchaseValidator.ValidatedGiftBox validated = validator.validate(configId, userId, 1);

        assertEquals(new BigDecimal("135.00"), validated.unitPrice());
        assertEquals(5, validated.items().size());
    }

    @Test
    void allowsSavingAnIncompleteDraftBeforeThePurchaseMinimumIsReached() {
        setItems(List.of(
                item(boxId, 1, CustomComponentType.BOX),
                item(firstContentId, 1, null)));

        GiftBoxPurchaseValidator.ValidatedGiftBox validated = validator.validateForSave(configId, userId);

        assertEquals(2, validated.items().size());
    }

    @Test
    void rejectsWhenRequestedBoxQuantityExceedsAnyComponentStock() {
        variants.get(thirdContentId).setStockQuantity(1);
        setItems(validItemsWithCard());

        assertThrows(BadRequestException.class, () -> validator.validate(configId, userId, 2));
    }

    @Test
    void rejectsContentsThatExceedTheBoxesMaximumWeight() {
        ProductVariant box = variants.get(boxId);
        box.setGiftBoxVariantDetail(GiftBoxVariantDetail.builder()
                .maxWeight(new BigDecimal("0.50"))
                .weightUnit("kg")
                .build());
        setItems(List.of(
                item(boxId, 1, CustomComponentType.BOX),
                item(firstContentId, 1, null),
                item(secondContentId, 1, null),
                item(thirdContentId, 1, null)));

        assertThrows(BadRequestException.class, () -> validator.validate(configId, userId, 1));
    }

    @Test
    void rejectsAProductThatDoesNotFitInsideTheBox() {
        ProductVariant box = variants.get(boxId);
        box.setGiftBoxVariantDetail(GiftBoxVariantDetail.builder()
                .innerLength(10.0)
                .innerWidth(10.0)
                .innerHeight(10.0)
                .dimensionUnit("cm")
                .build());
        variants.get(firstContentId).setLength(11.0);
        setItems(List.of(
                item(boxId, 1, CustomComponentType.BOX),
                item(firstContentId, 1, null),
                item(secondContentId, 1, null),
                item(thirdContentId, 1, null)));

        assertThrows(BadRequestException.class, () -> validator.validate(configId, userId, 1));
    }

    @Test
    void rejectsAConfigurationOwnedByAnotherUser() {
        config.setUserId(UUID.randomUUID());
        setItems(validItemsWithCard());

        assertThrows(BadRequestException.class, () -> validator.validate(configId, userId, 1));
    }

    private List<GiftBoxConfigItem> validItemsWithCard() {
        return List.of(
                item(boxId, 1, CustomComponentType.BOX),
                item(firstContentId, 1, null),
                item(secondContentId, 1, null),
                item(thirdContentId, 1, null),
                item(cardId, 1, CustomComponentType.GREETING_CARD));
    }

    private void setItems(List<GiftBoxConfigItem> items) {
        lenient().when(giftBoxConfigItemRepository.findByConfigId(configId)).thenReturn(items);
    }

    private GiftBoxConfigItem item(UUID variantId, int quantity, CustomComponentType type) {
        return GiftBoxConfigItem.builder()
                .config(config)
                .productVariantId(variantId)
                .quantity(quantity)
                .componentType(type)
                .build();
    }

    private ProductVariant variant(
            UUID id, ProductType type, CustomComponentType componentType, String name, String price) {
        Product product = Product.builder()
                .name(name)
                .productType(type)
                .customComponentType(componentType)
                .status(ProductStatus.AVAILABLE)
                .isCustomBoxEligible(true)
                .build();
        return ProductVariant.builder()
                .id(id)
                .product(product)
                .sku(name.replace(' ', '-').toUpperCase())
                .price(new BigDecimal(price))
                .stockQuantity(10)
                .status(VariantStatus.AVAILABLE)
                .length(1.0)
                .width(1.0)
                .height(1.0)
                .dimensionUnit("cm")
                .weight(0.2)
                .weightUnit("kg")
                .build();
    }
}