package elin.le.giftly.payment.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.payment.dto.CreatePaymentRequest;
import elin.le.giftly.payment.dto.PaymentResponse;
import elin.le.giftly.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management APIs for users")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create payment", description = "Create a new payment for an order. Supports COD and ZaloPay methods.")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Parameter(description = "Payment creation data", required = true)
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment created successfully", payment));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payments by order", description = "Retrieve all payments for a specific order")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByOrder(
            @Parameter(description = "Order ID", required = true)
            @PathVariable UUID orderId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", payments));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID", description = "Retrieve details of a specific payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable UUID paymentId) {
        PaymentResponse payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    @PostMapping("/{paymentId}/zalopay/callback")
    @Operation(summary = "ZaloPay callback", description = "Handle ZaloPay payment callback (success/failure)")
    public ResponseEntity<ApiResponse<PaymentResponse>> handleZaloPayCallback(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable UUID paymentId,
            @Parameter(description = "Payment success status", required = true)
            @RequestParam boolean success) {
        PaymentResponse payment = paymentService.handleZaloPayCallback(paymentId, success);
        String message = success ? "Payment successful" : "Payment failed";
        return ResponseEntity.ok(ApiResponse.success(message, payment));
    }
}
