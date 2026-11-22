package elin.le.giftly.payment.service;

import elin.le.giftly.common.enums.PaymentMethod;
import elin.le.giftly.common.enums.PaymentStage;
import elin.le.giftly.common.enums.PaymentStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.order.entity.Order;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.payment.dto.CreatePaymentRequest;
import elin.le.giftly.payment.dto.PaymentResponse;
import elin.le.giftly.payment.entity.Payment;
import elin.le.giftly.payment.repository.PaymentRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        UUID userId = getCurrentUserId();

        // Get and validate order
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.orderId()));

        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Order does not belong to you");
        }

        // Check if payment already exists for this order
        List<Payment> existingPayments = paymentRepository.findByOrderId(order.getId());
        for (Payment payment : existingPayments) {
            if (payment.getPaymentStatus() == PaymentStatus.PAID) {
                throw new BadRequestException("Order has already been paid");
            }
        }

        // Determine payment stage
        PaymentStage paymentStage = PaymentStage.FULL_PAYMENT;
        if (order.getRequiredAdvanceAmount() != null && order.getRequiredAdvanceAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            boolean hasAdvance = existingPayments.stream()
                    .anyMatch(p -> p.getPaymentStage() == PaymentStage.ADVANCE_PAYMENT);
            if (!hasAdvance) {
                paymentStage = PaymentStage.ADVANCE_PAYMENT;
            } else {
                paymentStage = PaymentStage.REMAINING_PAYMENT;
            }
        }

        // Create payment record
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .paymentStage(paymentStage)
                .amount(order.getTotalAmount())
                .build();

        payment = paymentRepository.save(payment);

        // Handle payment method specific logic
        String checkoutUrl = null;
        if (request.paymentMethod() == PaymentMethod.COD) {
            // COD: mark as pending, wait for delivery
            log.info("COD payment created for order: {}", order.getOrderNumber());
        } else if (request.paymentMethod() == PaymentMethod.ZALOPAY) {
            // ZaloPay: generate checkout URL
            checkoutUrl = generateZaloPayCheckoutUrl(payment);
            log.info("ZaloPay checkout URL generated for order: {}", order.getOrderNumber());
        }

        return toPaymentResponse(payment, checkoutUrl);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrder(UUID orderId) {
        UUID userId = getCurrentUserId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Order does not belong to you");
        }

        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
                .map(p -> toPaymentResponse(p, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId) {
        UUID userId = getCurrentUserId();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (!payment.getOrder().getUserId().equals(userId)) {
            throw new BadRequestException("Payment does not belong to you");
        }

        return toPaymentResponse(payment, null);
    }

    /**
     * Handle ZaloPay callback/return
     * In production, this would verify the signature and update payment status
     */
    @Transactional
    public PaymentResponse handleZaloPayCallback(UUID paymentId, boolean success) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getPaymentMethod() != PaymentMethod.ZALOPAY) {
            throw new BadRequestException("Payment is not a ZaloPay payment");
        }

        if (success) {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }

        payment = paymentRepository.save(payment);
        return toPaymentResponse(payment, null);
    }

    private PaymentResponse toPaymentResponse(Payment payment, String checkoutUrl) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getPaymentStage(),
                payment.getAmount(),
                payment.getPaidAt(),
                checkoutUrl
        );
    }

    /**
     * Generate ZaloPay checkout URL
     * In production, this would call ZaloPay API with proper credentials
     */
    private String generateZaloPayCheckoutUrl(Payment payment) {
        // TODO: Integrate with ZaloPay API
        // For now, return a placeholder URL
        log.warn("ZaloPay integration not implemented - returning placeholder URL");
        return "/api/payments/" + payment.getId() + "/zalopay/callback";
    }

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }
}
