package elin.le.giftly.order.service;

import elin.le.giftly.cart.entity.Cart;
import elin.le.giftly.cart.entity.CartItem;
import elin.le.giftly.cart.repository.CartItemRepository;
import elin.le.giftly.cart.repository.CartRepository;
import elin.le.giftly.common.enums.OrderStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.order.dto.CreateOrderRequest;
import elin.le.giftly.order.dto.OrderResponse;
import elin.le.giftly.order.entity.Order;
import elin.le.giftly.order.repository.OrderItemRepository;
import elin.le.giftly.order.repository.OrderRepository;
import elin.le.giftly.product.entity.Category;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.user.entity.Address;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.AddressRepository;
import elin.le.giftly.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private AddressRepository addressRepository;
    @InjectMocks private OrderService orderService;

    private void mockCurrentUser(User user) {
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(auth.getName()).thenReturn("test@example.com");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    }

    private Product createProduct() {
        Category cat = Category.builder().id(UUID.randomUUID()).name("Test").build();
        Product p = Product.builder().id(UUID.randomUUID()).category(cat).name("Test").price(new BigDecimal("100000")).stockQuantity(100).build();
        p.setImages(List.of());
        return p;
    }

    private Address createAddress(UUID userId) {
        Address a = Address.builder().id(UUID.randomUUID()).userId(userId).recipientName("Test").phone("0123").addressLine("123 St").ward("W").district("D").city("C").build();
        return a;
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrder() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        Product product = createProduct();
        Address address = createAddress(user.getId());
        Cart cart = Cart.builder().id(UUID.randomUUID()).userId(user.getId()).build();
        CartItem cartItem = CartItem.builder().id(UUID.randomUUID()).cart(cart).product(product).quantity(1).build();

        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(any())).thenReturn(List.of(cartItem));
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.createOrder(new CreateOrderRequest(address.getId()));
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING_CONFIRMATION, response.status());
    }

    @Test
    @DisplayName("Should throw when cart is empty")
    void shouldThrowWhenCartEmpty() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        Address address = createAddress(user.getId());
        Cart cart = Cart.builder().id(UUID.randomUUID()).userId(user.getId()).build();
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(any())).thenReturn(List.of());
        assertThrows(BadRequestException.class, () -> orderService.createOrder(new CreateOrderRequest(address.getId())));
    }

    @Test
    @DisplayName("Should cancel order")
    void shouldCancelOrder() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        Address address = createAddress(user.getId());
        Order order = Order.builder().id(UUID.randomUUID()).userId(user.getId()).status(OrderStatus.PENDING_CONFIRMATION).address(address).orderedAt(LocalDateTime.now()).build();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(any())).thenReturn(List.of());
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        OrderResponse response = orderService.cancelOrder(order.getId());
        assertEquals(OrderStatus.CANCELLED, response.status());
    }

    @Test
    @DisplayName("Should throw when order not cancellable")
    void shouldThrowWhenNotCancellable() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        Address address = createAddress(user.getId());
        Order order = Order.builder().id(UUID.randomUUID()).userId(user.getId()).status(OrderStatus.COMPLETED).address(address).orderedAt(LocalDateTime.now()).build();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        assertThrows(BadRequestException.class, () -> orderService.cancelOrder(order.getId()));
    }
}
