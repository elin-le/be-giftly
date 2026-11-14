package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.AdminOrderResponse;
import elin.le.giftly.admin.dto.UpdateOrderStatusRequest;
import elin.le.giftly.admin.service.AdminOrderService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.common.enums.OrderStatus;
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
    public ResponseEntity<ApiResponse<Page<AdminOrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<AdminOrderResponse> orders = adminOrderService.getAllOrders(status, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<AdminOrderResponse>> getOrderById(@PathVariable UUID orderId) {
        AdminOrderResponse order = adminOrderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<AdminOrderResponse>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        AdminOrderResponse order = adminOrderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<AdminOrderResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        AdminOrderResponse order = adminOrderService.updateOrderStatus(orderId, request.status());
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }
}
