package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.AdminOrderResponse;
import elin.le.giftly.admin.dto.UpdateOrderStatusRequest;
import elin.le.giftly.admin.service.AdminOrderService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.common.dto.PageResponse;
import elin.le.giftly.common.enums.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin - Orders", description = "Admin APIs for order management and status updates. Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve all orders with optional filtering by status or user with pagination")
    public ResponseEntity<ApiResponse<PageResponse<AdminOrderResponse>>> getAllOrders(
            @Parameter(description = "Filter by order status")
            @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Filter by user ID")
            @RequestParam(required = false) UUID userId,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {

        Page<AdminOrderResponse> orders = adminOrderService.getAllOrders(status, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(orders)));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
    public ResponseEntity<ApiResponse<AdminOrderResponse>> getOrderById(
            @Parameter(description = "Order ID", required = true)
            @PathVariable UUID orderId) {
        AdminOrderResponse order = adminOrderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number", description = "Retrieve a specific order by its order number string")
    public ResponseEntity<ApiResponse<AdminOrderResponse>> getOrderByOrderNumber(
            @Parameter(description = "Order number", required = true)
            @PathVariable String orderNumber) {
        AdminOrderResponse order = adminOrderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update the status of an order (e.g., PROCESSING, SHIPPED, DELIVERED)")
    public ResponseEntity<ApiResponse<AdminOrderResponse>> updateOrderStatus(
            @Parameter(description = "Order ID", required = true)
            @PathVariable UUID orderId,
            @Parameter(description = "New order status", required = true)
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        AdminOrderResponse order = adminOrderService.updateOrderStatus(orderId, request.status());
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }
}
