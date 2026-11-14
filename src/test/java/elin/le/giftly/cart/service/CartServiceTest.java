package elin.le.giftly.cart.service;

import elin.le.giftly.cart.dto.AddCartItemRequest;
import elin.le.giftly.cart.dto.CartResponse;
import elin.le.giftly.cart.dto.UpdateCartItemQuantityRequest;
import elin.le.giftly.cart.entity.Cart;
import elin.le.giftly.cart.entity.CartItem;
import elin.le.giftly.cart.repository.CartItemRepository;
import elin.le.giftly.cart.repository.CartRepository;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.Category;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.user.entity.User;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private CartService cartService;

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
        Product p = Product.builder().id(UUID.randomUUID()).category(cat).name("Test").price(new BigDecimal("100000")).stockQuantity(100).status(ProductStatus.AVAILABLE).build();
        p.setImages(List.of());
        return p;
    }

    @Test
    @DisplayName("Should return existing cart")
    void shouldReturnExistingCart() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        Cart cart = Cart.builder().id(UUID.randomUUID()).userId(user.getId()).build();
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(any())).thenReturn(List.of());
        assertNotNull(cartService.getCart());
    }

    @Test
    @DisplayName("Should add item to cart")
    void shouldAddItemToCart() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        Product product = createProduct();
        Cart cart = Cart.builder().id(UUID.randomUUID()).userId(user.getId()).build();
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(any(), any())).thenReturn(Optional.empty());
        when(cartItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(cartItemRepository.findByCartId(any())).thenReturn(List.of());
        assertNotNull(cartService.addCartItem(new AddCartItemRequest(product.getId(), 1)));
    }

    @Test
    @DisplayName("Should throw when product not found")
    void shouldThrowWhenProductNotFound() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        Cart cart = Cart.builder().id(UUID.randomUUID()).userId(user.getId()).build();
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cartService.addCartItem(new AddCartItemRequest(UUID.randomUUID(), 1)));
    }

    @Test
    @DisplayName("Should throw when insufficient stock")
    void shouldThrowWhenInsufficientStock() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        Product product = createProduct();
        product.setStockQuantity(0);
        Cart cart = Cart.builder().id(UUID.randomUUID()).userId(user.getId()).build();
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        assertThrows(BadRequestException.class, () -> cartService.addCartItem(new AddCartItemRequest(product.getId(), 1)));
    }

    @Test
    @DisplayName("Should update cart item quantity")
    void shouldUpdateQuantity() {
        Product product = createProduct();
        CartItem item = CartItem.builder().id(UUID.randomUUID()).product(product).quantity(1).build();
        when(cartItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(cartItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Cart cart = Cart.builder().id(UUID.randomUUID()).build();
        item.setCart(cart);
        when(cartItemRepository.findByCartId(any())).thenReturn(List.of(item));
        cartService.updateCartItemQuantity(item.getId(), new UpdateCartItemQuantityRequest(3));
        assertEquals(3, item.getQuantity());
    }

    @Test
    @DisplayName("Should remove cart item")
    void shouldRemoveItem() {
        Product product = createProduct();
        CartItem item = CartItem.builder().id(UUID.randomUUID()).product(product).quantity(1).build();
        Cart cart = Cart.builder().id(UUID.randomUUID()).build();
        item.setCart(cart);
        when(cartItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(cartItemRepository.findByCartId(any())).thenReturn(List.of());
        cartService.removeCartItem(item.getId());
        verify(cartItemRepository).delete(item);
    }

    @Test
    @DisplayName("Should clear cart")
    void shouldClearCart() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        Cart cart = Cart.builder().id(UUID.randomUUID()).userId(user.getId()).build();
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        cartService.clearCart();
        verify(cartItemRepository).deleteAllByCartId(cart.getId());
    }
}
