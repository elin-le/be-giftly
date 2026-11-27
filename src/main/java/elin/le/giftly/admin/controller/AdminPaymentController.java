package elin.le.giftly.admin.controller;

import elin.le.giftly.admin.dto.AdminPaymentResponse;
import elin.le.giftly.admin.dto.UpdatePaymentStatusRequest;
import elin.le.giftly.admin.service.AdminPaymentService;
import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.common.dto.PageResponse;
import elin.le.giftly.common.enums.PaymentStatus;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Payments", description = "Admin APIs for payment management. Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    @GetMapping
    @Operation(summary = "Get all payments", description = "Retrieve all payments with optional filtering by order or status with pagination")
    public ResponseEntity<ApiResponse<PageResponse<AdminPaymentResponse>>> getAllPayments(
            @Parameter(description = "Filter by order ID")
            @RequestParam(required = false) UUID orderId,
            @Parameter(description = "Filter by payment status")
            @RequestParam(required = false) PaymentStatus status,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {

        Page<AdminPaymentResponse> payments = adminPaymentService.getAllPayments(orderId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(payments)));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID", description = "Retrieve a specific payment by its ID")
    public ResponseEntity<ApiResponse<AdminPaymentResponse>> getPaymentById(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable UUID paymentId) {
        AdminPaymentResponse payment = adminPaymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payments by order", description = "Retrieve all payments for a specific order")
    public ResponseEntity<ApiResponse<List<AdminPaymentResponse>>> getPaymentsByOrder(
            @Parameter(description = "Order ID", required = true)
            @PathVariable UUID orderId) {
        List<AdminPaymentResponse> payments = adminPaymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", payments));
    }

    @PatchMapping("/{paymentId}/status")
    @Operation(summary = "Update payment status", description = "Update the status of a payment (e.g., PAID, FAILED, REFUNDED)")
    public ResponseEntity<ApiResponse<AdminPaymentResponse>> updatePaymentStatus(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable UUID paymentId,
            @Parameter(description = "New payment status", required = true)
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        AdminPaymentResponse payment = adminPaymentService.updatePaymentStatus(paymentId, request.status());
        return ResponseEntity.ok(ApiResponse.success("Payment status updated successfully", payment));
    }
}
