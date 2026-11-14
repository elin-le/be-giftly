package elin.le.giftly.payment.repository;

import elin.le.giftly.common.enums.PaymentStatus;
import elin.le.giftly.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrderId(UUID orderId);
    Payment findByOrderIdAndPaymentStatus(UUID orderId, PaymentStatus paymentStatus);
}
