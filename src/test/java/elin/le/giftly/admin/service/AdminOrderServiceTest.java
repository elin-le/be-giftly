package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.UpdateOrderStatusRequest;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.order.entity.Order;
import elin.le.giftly.order.repository.OrderItemRepository;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.user.entity.Address;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminOrderService Unit Tests")
class AdminOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminOrderService adminOrderService;

    private Order testOrder;
    private UUID testUserId;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testAddress = Address.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .recipientName("Test")
                .phone("0123")
                .addressLine("123 St")
                .ward("W")
                .district("D")
                .city("C")
                .build();
        testOrder = Order.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .orderNumber("ORD-123")
                .subtotal(new BigDecimal("100000"))
                .shippingFee(new BigDecimal("30000"))
                .totalAmount(new BigDecimal("130000"))
                .status(OrderStatus.PENDING_CONFIRMATION)
                .address(testAddress)
                .orderedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getAllOrders")
    class GetAllOrders {

        @Test
        @DisplayName("Should return all orders with no filters")
        void shouldReturnAllOrders() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> emptyPage = new PageImpl<>(List.of());
            when(orderRepository.findAll(pageable)).thenReturn(emptyPage);

            var result = adminOrderService.getAllOrders(null, null, pageable);

            assertNotNull(result);
            verify(orderRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should filter by status")
        void shouldFilterByStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> emptyPage = new PageImpl<>(List.of());
            when(orderRepository.findByStatus(OrderStatus.PENDING_CONFIRMATION, pageable)).thenReturn(emptyPage);

            adminOrderService.getAllOrders(OrderStatus.PENDING_CONFIRMATION, null, pageable);

            verify(orderRepository).findByStatus(OrderStatus.PENDING_CONFIRMATION, pageable);
        }

        @Test
        @DisplayName("Should filter by userId")
        void shouldFilterByUserId() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> emptyPage = new PageImpl<>(List.of());
            when(orderRepository.findByUserId(testUserId, pageable)).thenReturn(emptyPage);

            adminOrderService.getAllOrders(null, testUserId, pageable);

            verify(orderRepository).findByUserId(testUserId, pageable);
        }

        @Test
        @DisplayName("Should filter by both status and userId")
        void shouldFilterByBoth() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> emptyPage = new PageImpl<>(List.of());
            when(orderRepository.findByUserIdAndStatus(testUserId, OrderStatus.PENDING_CONFIRMATION, pageable))
                    .thenReturn(emptyPage);

            adminOrderService.getAllOrders(OrderStatus.PENDING_CONFIRMATION, testUserId, pageable);

            verify(orderRepository).findByUserIdAndStatus(testUserId, OrderStatus.PENDING_CONFIRMATION, pageable);
        }
    }

    @Nested
    @DisplayName("getOrderById")
    class GetOrderById {

        @Test
        @DisplayName("Should return order by ID")
        void shouldReturnOrderById() {
            when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));
            when(orderItemRepository.findByOrderId(any())).thenReturn(List.of());
            User user = User.builder().id(testUserId).email("test@example.com").fullName("Test").build();
            when(userRepository.findById(any())).thenReturn(Optional.of(user));

            var response = adminOrderService.getOrderById(testOrder.getId());

            assertNotNull(response);
            assertEquals(testOrder.getId(), response.id());
        }

        @Test
        @DisplayName("Should throw when order not found")
        void shouldThrowWhenNotFound() {
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> adminOrderService.getOrderById(orderId));
        }
    }

    @Nested
    @DisplayName("getOrderByOrderNumber")
    class GetOrderByOrderNumber {

        @Test
        @DisplayName("Should return order by order number")
        void shouldReturnOrderByNumber() {
            when(orderRepository.findByOrderNumber("ORD-123")).thenReturn(Optional.of(testOrder));
            when(orderItemRepository.findByOrderId(any())).thenReturn(List.of());
            User user = User.builder().id(testUserId).email("test@example.com").fullName("Test").build();
            when(userRepository.findById(any())).thenReturn(Optional.of(user));

            var response = adminOrderService.getOrderByOrderNumber("ORD-123");

            assertNotNull(response);
            assertEquals("ORD-123", response.orderNumber());
        }

        @Test
        @DisplayName("Should throw when order number not found")
        void shouldThrowWhenNotFound() {
            when(orderRepository.findByOrderNumber("NONEXISTENT")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> adminOrderService.getOrderByOrderNumber("NONEXISTENT"));
        }
    }

    @Nested
    @DisplayName("updateOrderStatus")
    class UpdateOrderStatus {

        @Test
        @DisplayName("Should update order status with valid transition")
        void shouldUpdateWithValidTransition() {
            testOrder.setStatus(OrderStatus.PENDING_CONFIRMATION);
            when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
            when(orderItemRepository.findByOrderId(any())).thenReturn(List.of());
            User user = User.builder().id(testUserId).email("test@example.com").fullName("Test").build();
            when(userRepository.findById(any())).thenReturn(Optional.of(user));

            var response = adminOrderService.updateOrderStatus(testOrder.getId(), OrderStatus.CONFIRMED);

            assertEquals(OrderStatus.CONFIRMED, response.status());
            verify(orderRepository).save(testOrder);
        }

        @Test
        @DisplayName("Should throw when order not found")
        void shouldThrowWhenNotFound() {
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> adminOrderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED));
        }

        @Test
        @DisplayName("Should throw when status same as current")
        void shouldThrowWhenSameStatus() {
            testOrder.setStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));

            assertThrows(BadRequestException.class,
                    () -> adminOrderService.updateOrderStatus(testOrder.getId(), OrderStatus.CONFIRMED));
        }

        @Test
        @DisplayName("Should throw when transitioning from terminal state")
        void shouldThrowFromTerminalState() {
            testOrder.setStatus(OrderStatus.COMPLETED);
            when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));

            assertThrows(BadRequestException.class,
                    () -> adminOrderService.updateOrderStatus(testOrder.getId(), OrderStatus.SHIPPING));
        }

        @Test
        @DisplayName("Should throw for invalid transition")
        void shouldThrowForInvalidTransition() {
            testOrder.setStatus(OrderStatus.PENDING_CONFIRMATION);
            when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));

            assertThrows(BadRequestException.class,
                    () -> adminOrderService.updateOrderStatus(testOrder.getId(), OrderStatus.DELIVERED));
        }
    }
}
