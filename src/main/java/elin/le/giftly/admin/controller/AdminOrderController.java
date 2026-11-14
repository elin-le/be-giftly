package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.UpdateOrderStatusRequest;
import elin.le.giftly.admin.service.AdminOrderService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.order.entity.Order;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Order>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Order> orders;
        if (status != null && userId != null) {
            orders = adminOrderService.getOrdersByUserId(userId, pageable);
        } else if (status != null) {
            orders = adminOrderService.getOrdersByStatus(status, pageable);
        } else if (userId != null) {
            orders = adminOrderService.getOrdersByUserId(userId, pageable);
        } else {
            orders = adminOrderService.getAllOrders(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable UUID orderId) {
        Order order = adminOrderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<Order>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        Order order = adminOrderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        Order order = adminOrderService.updateOrderStatus(orderId, request.status());
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }
}
