package elin.le.giftly.order.service;

import elin.le.giftly.cart.entity.Cart;
import elin.le.giftly.cart.entity.CartItem;
import elin.le.giftly.cart.repository.CartItemRepository;
import elin.le.giftly.cart.repository.CartRepository;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.order.dto.*;
import elin.le.giftly.order.entity.Order;
import elin.le.giftly.order.entity.OrderItem;
import elin.le.giftly.order.repository.OrderItemRepository;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.repository.ProductRepository;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
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
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            if (cartItem.getProduct() != null) {
                Product product = cartItem.getProduct();
                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    throw new BadRequestException("Insufficient stock for: " + product.getName());
                }

                BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                subtotal = subtotal.add(itemSubtotal);

                OrderItem orderItem = OrderItem.builder()
                        .product(product)
                        .itemName(product.getName())
                        .unitPrice(product.getPrice())
                        .quantity(cartItem.getQuantity())
                        .subtotal(itemSubtotal)
                        .build();
                orderItems.add(orderItem);
            }
        }

        // Create order
        String orderNumber = generateOrderNumber();
        Order order = Order.builder()
                .userId(userId)
                .address(address)
                .orderNumber(orderNumber)
                .subtotal(subtotal)
                .shippingFee(SHIPPING_FEE)
                .totalAmount(subtotal.add(SHIPPING_FEE))
                .status(OrderStatus.PENDING_CONFIRMATION)
                .orderedAt(LocalDateTime.now())
                .items(orderItems)
                .build();

        // Set order reference on items
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }

        order = orderRepository.save(order);

        // Reduce stock
        for (OrderItem item : orderItems) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepository.save(product);
            }
        }

        // Clear cart
        cartItemRepository.deleteAllByCartId(cart.getId());

        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public PaginatedOrdersResponse getMyOrders(String cursor, int pageSize) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        int limit = (pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE) + 1;
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "orderedAt"));

        UUID cursorDate = null; // Using orderedAt for cursor
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

        // Only allow cancellation when order is in PENDING_CONFIRMATION status
        if (order.getStatus() != OrderStatus.PENDING_CONFIRMATION) {
            throw new BadRequestException(
                    "Order can only be cancelled before admin confirmation. Current status: " + order.getStatus()
            );
        }

        // Restore stock for all order items
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        // Update order status to CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct() != null ? item.getProduct().getId() : null,
                        item.getItemName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
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
                order.getStatus(),
                order.getOrderedAt(),
                addressResponse,
                itemResponses
        );
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private UUID getCurrentUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }
}
