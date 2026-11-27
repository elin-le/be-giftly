package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.AdminPaymentResponse;
import elin.le.giftly.common.enums.PaymentStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.order.entity.Order;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.payment.entity.Payment;
import elin.le.giftly.payment.repository.PaymentRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AdminPaymentResponse> getAllPayments(UUID orderId, PaymentStatus status, Pageable pageable) {
        if (orderId != null && status != null) {
            List<Payment> payments = paymentRepository.findByOrderId(orderId).stream()
                    .filter(p -> p.getPaymentStatus() == status)
                    .toList();
            // Simple pagination for filtered results
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), payments.size());
            List<AdminPaymentResponse> pageContent = payments.subList(start, end).stream()
                    .map(this::toAdminResponse)
                    .toList();
            return new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, payments.size());
        }
        if (orderId != null) {
            List<Payment> payments = paymentRepository.findByOrderId(orderId);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), payments.size());
            List<AdminPaymentResponse> pageContent = payments.subList(start, end).stream()
                    .map(this::toAdminResponse)
                    .toList();
            return new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, payments.size());
        }
        if (status != null) {
            List<Payment> payments = paymentRepository.findByPaymentStatus(status);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), payments.size());
            List<AdminPaymentResponse> pageContent = payments.subList(start, end).stream()
                    .map(this::toAdminResponse)
                    .toList();
            return new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, payments.size());
        }
        return paymentRepository.findAll(pageable).map(this::toAdminResponse);
    }

    @Transactional(readOnly = true)
    public AdminPaymentResponse getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        return toAdminResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<AdminPaymentResponse> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Transactional
    public AdminPaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getPaymentStatus() == newStatus) {
            throw new BadRequestException("Payment is already in status: " + newStatus);
        }

        // Terminal states cannot be changed
        if (payment.getPaymentStatus() == PaymentStatus.PAID ||
            payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new BadRequestException(
                "Cannot change status from terminal state: " + payment.getPaymentStatus()
            );
        }

        payment.setPaymentStatus(newStatus);
        if (newStatus == PaymentStatus.PAID && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }

        payment = paymentRepository.save(payment);
        return toAdminResponse(payment);
    }

    private AdminPaymentResponse toAdminResponse(Payment payment) {
        Order order = orderRepository.findById(payment.getOrder().getId()).orElse(null);
        User user = order != null ? userRepository.findById(order.getUserId()).orElse(null) : null;

        return new AdminPaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                order != null ? order.getOrderNumber() : null,
                order != null ? order.getUserId() : null,
                user != null ? user.getEmail() : null,
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getPaymentStage(),
                payment.getAmount(),
                payment.getPaidAt()
        );
    }
}
