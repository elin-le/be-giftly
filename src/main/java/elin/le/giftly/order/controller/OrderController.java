package elin.le.giftly.order.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.order.dto.CreateOrderRequest;
import elin.le.giftly.order.dto.OrderResponse;
import elin.le.giftly.order.dto.PaginatedOrdersResponse;
import elin.le.giftly.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs for customers")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order", description = "Create a new order from the current user's shopping cart")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Parameter(description = "Order creation data with shipping info", required = true)
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", order));
    }

    @GetMapping
    @Operation(summary = "Get my orders", description = "Retrieve the current user's order history with cursor-based pagination")
    public ResponseEntity<ApiResponse<PaginatedOrdersResponse>> getMyOrders(
            @Parameter(description = "Cursor for pagination")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Number of items per page (default 20)")
            @RequestParam(defaultValue = "20") int pageSize) {
        PaginatedOrdersResponse orders = orderService.getMyOrders(cursor, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve details of a specific order belonging to the current user")
    public ResponseEntity<ApiResponse<OrderResponse>> getMyOrderById(
            @Parameter(description = "Order ID", required = true)
            @PathVariable UUID orderId) {
        OrderResponse order = orderService.getMyOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order that belongs to the current user")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @Parameter(description = "Order ID to cancel", required = true)
            @PathVariable UUID orderId) {
        OrderResponse order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
}
