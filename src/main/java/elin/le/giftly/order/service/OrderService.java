package elin.le.giftly.order.service;

import elin.le.giftly.cart.entity.Cart;
import elin.le.giftly.cart.entity.CartItem;
import elin.le.giftly.cart.repository.CartItemRepository;
import elin.le.giftly.cart.repository.CartRepository;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.common.enums.ComponentRole;
import elin.le.giftly.common.enums.CustomComponentType;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ReturnPolicy;
import elin.le.giftly.common.enums.VariantStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.giftbox.dto.PurchaseGiftBoxRequest;
import elin.le.giftly.giftbox.entity.GiftBoxConfigItem;
import elin.le.giftly.giftbox.service.GiftBoxPurchaseValidator;
import elin.le.giftly.order.dto.*;
import elin.le.giftly.order.entity.Order;
import elin.le.giftly.order.entity.OrderItem;
import elin.le.giftly.order.entity.OrderItemComponent;
import elin.le.giftly.order.repository.OrderItemRepository;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.entity.ProductVariantPolicy;
import elin.le.giftly.product.repository.ProductVariantPolicyRepository;
import elin.le.giftly.product.repository.ProductVariantRepository;
import elin.le.giftly.user.entity.Address;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.AddressRepository;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantPolicyRepository productVariantPolicyRepository;
    private final GiftBoxPurchaseValidator giftBoxPurchaseValidator;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final BigDecimal SHIPPING_FEE = BigDecimal.valueOf(30000);

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        // Get and validate cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Your cart is empty"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        // Validate address belongs to user
        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.addressId()));
        if (!address.getUserId().equals(userId)) {
            throw new BadRequestException("Address does not belong to you");
        }

        // Validate stock and build order items
        List<OrderItem> orderItems = new ArrayList<>();
        Map<UUID, Long> stockRequirements = new HashMap<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal maxAdvancePercent = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductVariantId() == null) {
                throw new BadRequestException("Cart item must contain a product variant");
            }

            ProductVariant variant = productVariantRepository.findById(cartItem.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", cartItem.getProductVariantId()));
            if (variant.getStatus() != VariantStatus.AVAILABLE
                    || variant.getProduct().getStatus() != ProductStatus.AVAILABLE) {
                throw new BadRequestException("Product variant is not available: " + variant.getSku());
            }

            reserveStock(variant, cartItem.getQuantity(), stockRequirements);

            BigDecimal itemSubtotal = variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            ProductVariantPolicy policy = productVariantPolicyRepository.findByVariantId(variant.getId());
            BigDecimal itemAdvancePercent = BigDecimal.ZERO;
            boolean itemHasNoReturn = false;
            if (policy != null && policy.getAdvancePaymentPercent() != null) {
                itemAdvancePercent = policy.getAdvancePaymentPercent();
                maxAdvancePercent = max(maxAdvancePercent, itemAdvancePercent);
                itemHasNoReturn = policy.getReturnPolicy() == ReturnPolicy.NO_RETURN;
            }

            OrderItem orderItem = OrderItem.builder()
                    .productVariantId(variant.getId())
                    .itemNameSnapshot(variant.getProduct().getName())
                    .skuSnapshot(variant.getSku())
                    .unitPrice(variant.getPrice())
                    .quantity(cartItem.getQuantity())
                    .subtotal(itemSubtotal)
                    .returnPolicySnapshot(itemHasNoReturn ? ReturnPolicy.NO_RETURN : ReturnPolicy.RETURNABLE)
                    .advancePaymentPercentSnapshot(itemAdvancePercent)
                    .requiredAdvanceAmount(itemSubtotal.multiply(itemAdvancePercent).divide(BigDecimal.valueOf(100)))
                    .build();
            orderItems.add(orderItem);
        }

        Order order = createOrderRecord(userId, address, subtotal, maxAdvancePercent, orderItems, stockRequirements);

        cartItemRepository.deleteAllByCartId(cart.getId());

        return toOrderResponse(order);
    }

    @Transactional
    public OrderResponse createGiftBoxOrder(UUID giftBoxConfigId, PurchaseGiftBoxRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);
        Address address = getUserAddress(request.addressId(), userId);
        GiftBoxPurchaseValidator.ValidatedGiftBox giftBox = giftBoxPurchaseValidator.validate(
                giftBoxConfigId, userId, request.quantity());

        BigDecimal subtotal = giftBox.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));
        BigDecimal maxAdvancePercent = BigDecimal.ZERO;
        boolean itemHasNoReturn = false;
        List<OrderItemComponent> components = new ArrayList<>();

        for (GiftBoxPurchaseValidator.ValidatedItem validatedItem : giftBox.items()) {
            ProductVariant variant = validatedItem.variant();
            ProductVariantPolicy policy = productVariantPolicyRepository.findByVariantId(variant.getId());
            if (policy != null) {
                if (policy.getAdvancePaymentPercent() != null) {
                    maxAdvancePercent = max(maxAdvancePercent, policy.getAdvancePaymentPercent());
                }
                if (policy.getReturnPolicy() == ReturnPolicy.NO_RETURN) {
                    itemHasNoReturn = true;
                }
            }

            GiftBoxConfigItem configItem = validatedItem.configItem();
            OrderItemComponent component = OrderItemComponent.builder()
                    .productVariantId(variant.getId())
                    .componentRole(toComponentRole(configItem, variant.getProduct().getProductType()))
                    .productNameSnapshot(variant.getProduct().getName())
                    .skuSnapshot(variant.getSku())
                    .quantity(configItem.getQuantity())
                    .unitPrice(validatedItem.unitPrice())
                    .isComplimentary(validatedItem.unitPrice().compareTo(BigDecimal.ZERO) == 0)
                    .messageText(configItem.getMessageText())
                    .messageRenderingMethod(configItem.getMessageRenderingMethod())
                    .isPerishableSnapshot(policy == null ? null : policy.getIsPerishable())
                    .shelfLifeHoursSnapshot(policy == null ? null : policy.getShelfLifeHours())
                    .returnPolicySnapshot(policy == null ? null : policy.getReturnPolicy())
                    .advancePaymentPercentSnapshot(policy == null ? null : policy.getAdvancePaymentPercent())
                    .build();
            components.add(component);
        }

        String boxName = giftBox.items().stream()
                .filter(item -> item.configItem().getComponentType() == CustomComponentType.BOX)
                .map(item -> item.variant().getProduct().getName())
                .findFirst()
                .orElse("Gift box");
        OrderItem orderItem = OrderItem.builder()
                .giftBoxConfigId(giftBox.config().getId())
                .itemNameSnapshot("Custom gift box - " + boxName)
                .unitPrice(giftBox.unitPrice())
                .quantity(request.quantity())
                .subtotal(subtotal)
                .returnPolicySnapshot(itemHasNoReturn ? ReturnPolicy.NO_RETURN : ReturnPolicy.RETURNABLE)
                .advancePaymentPercentSnapshot(maxAdvancePercent)
                .requiredAdvanceAmount(subtotal.multiply(maxAdvancePercent).divide(BigDecimal.valueOf(100)))
                .components(components)
                .build();
        components.forEach(component -> component.setOrderItem(orderItem));

        return toOrderResponse(createOrderRecord(
                userId,
                address,
                subtotal,
                maxAdvancePercent,
                List.of(orderItem),
                giftBox.stockRequirements()));
    }

    private Address getUserAddress(UUID addressId, UUID userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        if (!address.getUserId().equals(userId)) {
            throw new BadRequestException("Address does not belong to you");
        }
        return address;
    }

    private Order createOrderRecord(
            UUID userId,
            Address address,
            BigDecimal subtotal,
            BigDecimal maxAdvancePercent,
            List<OrderItem> orderItems,
            Map<UUID, Long> stockRequirements
    ) {
        BigDecimal requiredAdvance = subtotal.multiply(maxAdvancePercent).divide(BigDecimal.valueOf(100));
        Order order = Order.builder()
                .userId(userId)
                .address(address)
                .orderNumber(generateOrderNumber())
                .subtotal(subtotal)
                .shippingFee(SHIPPING_FEE)
                .totalAmount(subtotal.add(SHIPPING_FEE))
                .requiredAdvanceAmount(requiredAdvance)
                .status(OrderStatus.PENDING_CONFIRMATION)
                .orderedAt(LocalDateTime.now())
                .items(orderItems)
                .build();
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }

        order = orderRepository.save(order);
        for (Map.Entry<UUID, Long> stockRequirement : stockRequirements.entrySet()) {
            ProductVariant variant = productVariantRepository.findById(stockRequirement.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", stockRequirement.getKey()));
            variant.setStockQuantity(variant.getStockQuantity() - Math.toIntExact(stockRequirement.getValue()));
            productVariantRepository.save(variant);
        }
        return order;
    }

    @Transactional(readOnly = true)
    public PaginatedOrdersResponse getMyOrders(String cursor, int pageSize) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        int limit = (pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE) + 1;
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "orderedAt"));

        List<Order> orders;

        if (cursor != null && !cursor.isEmpty()) {
            LocalDateTime cursorTime = LocalDateTime.parse(cursor);
            orders = orderRepository.findByUserIdAndOrderedAtBefore(userId, cursorTime, limit);
        } else {
            orders = orderRepository.findTopByUserId(userId, pageRequest);
        }

        boolean hasMore = orders.size() > limit - 1;
        List<Order> pagedOrders = hasMore ? orders.subList(0, limit - 1) : orders;

        List<OrderResponse> orderResponses = pagedOrders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());

        String nextCursor = null;
        if (hasMore && !pagedOrders.isEmpty()) {
            nextCursor = pagedOrders.get(pagedOrders.size() - 1).getOrderedAt().toString();
        }

        return new PaginatedOrdersResponse(orderResponses, nextCursor, hasMore, pagedOrders.size());
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrderById(UUID orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("You do not have access to this order");
        }

        return toOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("You do not have access to this order");
        }

        if (order.getStatus() != OrderStatus.PENDING_CONFIRMATION) {
            throw new BadRequestException(
                    "Order can only be cancelled before admin confirmation. Current status: " + order.getStatus()
            );
        }

        // Restore stock
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            if (item.getProductVariantId() != null) {
                ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", item.getProductVariantId()));
                variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                productVariantRepository.save(variant);
            }
            for (OrderItemComponent component : item.getComponents()) {
                if (component.getProductVariantId() == null || component.getQuantity() == null) {
                    continue;
                }
                ProductVariant variant = productVariantRepository.findById(component.getProductVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Product Variant", "id", component.getProductVariantId()));
                int restoredQuantity = Math.toIntExact((long) component.getQuantity() * item.getQuantity());
                variant.setStockQuantity(variant.getStockQuantity() + restoredQuantity);
                productVariantRepository.save(variant);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductVariantId(),
                        item.getGiftBoxConfigId(),
                        item.getItemNameSnapshot(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal(),
                        item.getComponents().stream()
                                .map(component -> new OrderItemResponse.ComponentResponse(
                                        component.getProductVariantId(),
                                        component.getComponentRole(),
                                        component.getProductNameSnapshot(),
                                        component.getSkuSnapshot(),
                                        component.getQuantity(),
                                        component.getUnitPrice(),
                                        component.getIsComplimentary(),
                                        component.getMessageText(),
                                        component.getMessageRenderingMethod()))
                                .toList()
                ))
                .collect(Collectors.toList());

        OrderResponse.AddressResponse addressResponse = new OrderResponse.AddressResponse(
                order.getAddress().getId(),
                order.getAddress().getRecipientName(),
                order.getAddress().getPhone(),
                order.getAddress().getAddressLine(),
                order.getAddress().getWard(),
                order.getAddress().getDistrict(),
                order.getAddress().getCity()
        );

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getRequiredAdvanceAmount(),
                order.getStatus(),
                order.getOrderedAt(),
                addressResponse,
                itemResponses
        );
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private void reserveStock(ProductVariant variant, long quantity, Map<UUID, Long> stockRequirements) {
        long totalRequired = stockRequirements.merge(variant.getId(), quantity, Long::sum);
        if (totalRequired > variant.getStockQuantity()) {
            throw new BadRequestException("Insufficient stock for: " + variant.getProduct().getName()
                    + ". Available: " + variant.getStockQuantity());
        }
    }

    private BigDecimal max(BigDecimal first, BigDecimal second) {
        return first.compareTo(second) >= 0 ? first : second;
    }

    private ComponentRole toComponentRole(GiftBoxConfigItem item, ProductType productType) {
        if (item.getComponentType() == CustomComponentType.BOX) {
            return ComponentRole.BOX;
        }
        if (item.getComponentType() == CustomComponentType.GREETING_CARD
                || productType == ProductType.GREETING_CARD) {
            return ComponentRole.GREETING_CARD;
        }
        if (item.getComponentType() != null || productType == ProductType.PACKAGING_ACCESSORY) {
            return ComponentRole.ACCESSORY;
        }
        return ComponentRole.PRODUCT;
    }

    private UUID getCurrentUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }
}
