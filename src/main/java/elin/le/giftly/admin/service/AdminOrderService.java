package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.AdminOrderResponse;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.order.entity.Order;
import elin.le.giftly.order.entity.OrderItem;
import elin.le.giftly.order.repository.OrderItemRepository;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AdminOrderResponse> getAllOrders(OrderStatus status, UUID userId, Pageable pageable) {
        Page<Order> orders;
        if (status != null && userId != null) {
            orders = orderRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else if (userId != null) {
            orders = orderRepository.findByUserId(userId, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(this::toAdminResponse);
    }

    @Transactional(readOnly = true)
    public AdminOrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return toAdminResponse(order);
    }

    @Transactional(readOnly = true)
    public AdminOrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return toAdminResponse(order);
    }

    @Transactional
    public AdminOrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.setStatus(newStatus);
        order = orderRepository.save(order);
        return toAdminResponse(order);
    }

    private AdminOrderResponse toAdminResponse(Order order) {
        User user = userRepository.findById(order.getUserId()).orElse(null);

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<AdminOrderResponse.AdminOrderItemResponse> itemResponses = items.stream()
            .map(item -> new AdminOrderResponse.AdminOrderItemResponse(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getItemName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal()
            ))
            .collect(Collectors.toList());

        AdminOrderResponse.AddressResponse addressResponse = new AdminOrderResponse.AddressResponse(
            order.getAddress().getId(),
            order.getAddress().getRecipientName(),
            order.getAddress().getPhone(),
            order.getAddress().getAddressLine(),
            order.getAddress().getWard(),
            order.getAddress().getDistrict(),
            order.getAddress().getCity()
        );

        return new AdminOrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getUserId(),
            user != null ? user.getEmail() : null,
            user != null ? user.getFullName() : null,
            order.getSubtotal(),
            order.getShippingFee(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getOrderedAt(),
            addressResponse,
            itemResponses
        );
    }
}
