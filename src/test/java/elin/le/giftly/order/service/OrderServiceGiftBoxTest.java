package elin.le.giftly.order.service;

import elin.le.giftly.cart.repository.CartItemRepository;
import elin.le.giftly.cart.repository.CartRepository;
import elin.le.giftly.common.enums.CustomComponentType;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.VariantStatus;
import elin.le.giftly.giftbox.entity.GiftBoxConfig;
import elin.le.giftly.giftbox.entity.GiftBoxConfigItem;
import elin.le.giftly.giftbox.service.GiftBoxPurchaseValidator;
import elin.le.giftly.giftbox.dto.PurchaseGiftBoxRequest;
import elin.le.giftly.order.dto.OrderResponse;
import elin.le.giftly.order.entity.Order;
import elin.le.giftly.order.repository.OrderItemRepository;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.ProductVariantPolicyRepository;
import elin.le.giftly.product.repository.ProductVariantRepository;
import elin.le.giftly.user.entity.Address;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.AddressRepository;
import elin.le.giftly.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceGiftBoxTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private ProductVariantPolicyRepository productVariantPolicyRepository;
    @Mock
    private GiftBoxPurchaseValidator giftBoxPurchaseValidator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AddressRepository addressRepository;
    @InjectMocks
    private OrderService orderService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ordersCustomGiftBoxSnapshotsComponentsAndRestoresTheirStockOnCancellation() {
        UUID userId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String email = "giftbox-buyer@example.test";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "password"));

        GiftBoxConfig config = GiftBoxConfig.builder().id(configId).userId(userId).build();
        Address address = Address.builder().id(addressId).userId(userId).recipientName("Gift Buyer")
                .phone("0900000000").addressLine("123 Main Street").build();
        Map<UUID, ProductVariant> variants = new HashMap<>();
        List<GiftBoxPurchaseValidator.ValidatedItem> validatedItems = new ArrayList<>();
        addComponent(variants, validatedItems, ProductType.READY_MADE_GIFT_BOX, CustomComponentType.BOX,
                "Gift box", "100.00");
        addComponent(variants, validatedItems, ProductType.REGULAR, null, "Gift item one", "10.00");
        addComponent(variants, validatedItems, ProductType.REGULAR, null, "Gift item two", "10.00");
        addComponent(variants, validatedItems, ProductType.REGULAR, null, "Gift item three", "10.00");
        addComponent(variants, validatedItems, ProductType.GREETING_CARD, CustomComponentType.GREETING_CARD,
                "Greeting card", "5.00");

        Map<UUID, Long> stockRequirements = new HashMap<>();
        variants.keySet().forEach(id -> stockRequirements.put(id, 2L));
        GiftBoxPurchaseValidator.ValidatedGiftBox validatedGiftBox = new GiftBoxPurchaseValidator.ValidatedGiftBox(
                config, validatedItems, new BigDecimal("135.00"), stockRequirements);
        AtomicReference<Order> savedOrder = new AtomicReference<>();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(User.builder().id(userId).email(email).build()));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(giftBoxPurchaseValidator.validate(configId, userId, 2)).thenReturn(validatedGiftBox);
        when(productVariantRepository.findById(any(UUID.class))).thenAnswer(invocation ->
                Optional.ofNullable(variants.get(invocation.getArgument(0))));
        when(productVariantPolicyRepository.findByVariantId(any(UUID.class))).thenReturn(null);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId);
            savedOrder.set(order);
            return order;
        });
        when(orderRepository.findById(orderId)).thenAnswer(invocation -> Optional.ofNullable(savedOrder.get()));
        when(orderItemRepository.findByOrderId(orderId)).thenAnswer(invocation -> savedOrder.get().getItems());

        OrderResponse response = orderService.createGiftBoxOrder(configId, new PurchaseGiftBoxRequest(addressId, 2));

        assertEquals(new BigDecimal("270.00"), response.subtotal());
        assertEquals(1, response.items().size());
        assertEquals(configId, response.items().getFirst().giftBoxConfigId());
        assertEquals(5, response.items().getFirst().components().size());
        variants.values().forEach(variant -> assertEquals(8, variant.getStockQuantity()));
        verifyNoInteractions(cartRepository, cartItemRepository);

        OrderResponse cancelledOrder = orderService.cancelOrder(orderId);

        assertEquals(OrderStatus.CANCELLED, cancelledOrder.status());
        variants.values().forEach(variant -> assertEquals(10, variant.getStockQuantity()));
    }

    private void addComponent(
            Map<UUID, ProductVariant> variants,
            List<GiftBoxPurchaseValidator.ValidatedItem> validatedItems,
            ProductType productType,
            CustomComponentType componentType,
            String name,
            String price
    ) {
        UUID variantId = UUID.randomUUID();
        Product product = Product.builder()
                .name(name)
                .productType(productType)
                .customComponentType(componentType)
                .status(ProductStatus.AVAILABLE)
                .isCustomBoxEligible(true)
                .build();
        ProductVariant variant = ProductVariant.builder()
                .id(variantId)
                .product(product)
                .sku(name.replace(' ', '-').toUpperCase())
                .price(new BigDecimal(price))
                .stockQuantity(10)
                .status(VariantStatus.AVAILABLE)
                .build();
        GiftBoxConfigItem configItem = GiftBoxConfigItem.builder()
                .productVariantId(variantId)
                .quantity(1)
                .componentType(componentType)
                .build();
        variants.put(variantId, variant);
        validatedItems.add(new GiftBoxPurchaseValidator.ValidatedItem(
                configItem, variant, new BigDecimal(price)));
    }
}