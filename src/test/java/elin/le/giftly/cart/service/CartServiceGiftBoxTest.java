package elin.le.giftly.cart.service;

import elin.le.giftly.cart.dto.AddCartItemRequest;
import elin.le.giftly.cart.dto.CartResponse;
import elin.le.giftly.cart.entity.Cart;
import elin.le.giftly.cart.repository.CartItemRepository;
import elin.le.giftly.cart.repository.CartRepository;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.VariantStatus;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.ProductVariantRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceGiftBoxTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CartService cartService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cartContainsOnlyRegularProductVariants() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();
        String email = "giftbox-buyer@example.test";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "password"));

        User user = User.builder().id(userId).email(email).build();
        Cart cart = Cart.builder().id(cartId).userId(userId).build();
        Product product = Product.builder().name("Gấu bông Teddy").status(ProductStatus.AVAILABLE).build();
        ProductVariant variant = ProductVariant.builder()
                .id(variantId)
                .product(product)
                .sku("TEDDY-01")
                .price(new BigDecimal("135.00"))
                .stockQuantity(10)
                .status(VariantStatus.AVAILABLE)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(cartItemRepository.findByCartIdAndProductVariantId(cartId, variantId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any())).thenAnswer(invocation -> {
            var item = invocation.<elin.le.giftly.cart.entity.CartItem>getArgument(0);
            item.setId(cartItemId);
            return item;
        });
        when(cartItemRepository.findByCartId(cartId)).thenAnswer(invocation -> cart.getItems());

        CartResponse response = cartService.addCartItem(new AddCartItemRequest(variantId, 1));

        assertEquals(1, response.items().size());
        assertEquals(variantId, response.items().getFirst().productVariantId());
        assertEquals(new BigDecimal("135.00"), response.items().getFirst().price());
        assertEquals(new BigDecimal("135.00"), response.subtotal());
        assertEquals(1, response.totalItems());
    }
}