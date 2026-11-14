package elin.le.giftly.cart.service;

import elin.le.giftly.cart.dto.AddCartItemRequest;
import elin.le.giftly.cart.dto.CartItemResponse;
import elin.le.giftly.cart.dto.CartResponse;
import elin.le.giftly.cart.dto.UpdateCartItemQuantityRequest;
import elin.le.giftly.cart.entity.Cart;
import elin.le.giftly.cart.entity.CartItem;
import elin.le.giftly.cart.repository.CartItemRepository;
import elin.le.giftly.cart.repository.CartRepository;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().userId(userId).build();
                    return cartRepository.save(newCart);
                });

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse addCartItem(AddCartItemRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().userId(userId).build();
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.productId()));

        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new BadRequestException("Product is not available");
        }

        if (product.getStockQuantity() < request.quantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId());
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.quantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
            return toCartResponse(cart);
        }

        // Add new item
        CartItem newItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(request.quantity())
                .build();
        cartItemRepository.save(newItem);
        cart.getItems().add(newItem);

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItemQuantity(UUID itemId, UpdateCartItemQuantityRequest request) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item", "id", itemId));

        if (item.getProduct() != null) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < request.quantity()) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
            }
        }

        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        Cart cart = item.getCart();
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse removeCartItem(UUID itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item", "id", itemId));

        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return toCartResponse(cart);
    }

    @Transactional
    public void clearCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        cartItemRepository.deleteAllByCartId(cart.getId());
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItem item : items) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                String thumbnailUrl = getThumbnailUrl(product);
                BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

                itemResponses.add(new CartItemResponse(
                        item.getId(),
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        thumbnailUrl,
                        item.getQuantity()
                ));

                subtotal = subtotal.add(itemTotal);
                totalItems += item.getQuantity();
            }
        }

        return new CartResponse(cart.getId(), itemResponses, subtotal, totalItems);
    }

    private String getThumbnailUrl(Product product) {
        return product.getImages().stream()
                .filter(ProductImage::getIsThumbnail)
                .map(ProductImage::getSecureUrl)
                .findFirst()
                .orElseGet(() -> product.getImages().stream()
                        .map(ProductImage::getSecureUrl)
                        .findFirst()
                        .orElse(null));
    }

    private UUID getCurrentUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }
}
