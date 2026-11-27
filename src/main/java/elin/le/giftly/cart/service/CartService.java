package elin.le.giftly.cart.service;

import elin.le.giftly.cart.dto.AddCartItemRequest;
import elin.le.giftly.cart.dto.CartItemResponse;
import elin.le.giftly.cart.dto.CartResponse;
import elin.le.giftly.cart.dto.UpdateCartItemQuantityRequest;
import elin.le.giftly.cart.entity.Cart;
import elin.le.giftly.cart.entity.CartItem;
import elin.le.giftly.cart.repository.CartItemRepository;
import elin.le.giftly.cart.repository.CartRepository;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.ProductVariantRepository;
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
    private final ProductVariantRepository productVariantRepository;
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

        ProductVariant variant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", request.productVariantId()));

        if (variant.getStatus() != elin.le.giftly.common.enums.VariantStatus.AVAILABLE) {
            throw new BadRequestException("Product variant is not available");
        }

        if (variant.getStockQuantity() < request.quantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + variant.getStockQuantity());
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), request.productVariantId());
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.quantity();
            if (variant.getStockQuantity() < newQuantity) {
                throw new BadRequestException("Insufficient stock. Available: " + variant.getStockQuantity());
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
            return toCartResponse(cart);
        }

        // Add new item
        CartItem newItem = CartItem.builder()
                .cart(cart)
                .productVariantId(variant.getId())
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

        UUID userId = getCurrentUserId(SecurityContextHolder.getContext().getAuthentication().getName());
        if (!item.getCart().getUserId().equals(userId)) {
            throw new BadRequestException("You do not have access to this cart item");
        }

        if (item.getProductVariantId() == null) {
            throw new BadRequestException("Cart item must contain a product variant");
        }
        ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", item.getProductVariantId()));
        if (variant.getStockQuantity() < request.quantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + variant.getStockQuantity());
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
        UUID userId = getCurrentUserId(SecurityContextHolder.getContext().getAuthentication().getName());
        if (!cart.getUserId().equals(userId)) {
            throw new BadRequestException("You do not have access to this cart item");
        }
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
            if (item.getProductVariantId() == null) {
                throw new BadRequestException("Cart item must contain a product variant");
            }

            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", item.getProductVariantId()));
            Product product = variant.getProduct();
            String thumbnailUrl = getThumbnailUrl(variant);
            BigDecimal itemTotal = variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            itemResponses.add(new CartItemResponse(
                    item.getId(),
                    variant.getId(),
                    product.getName(),
                    variant.getPrice(),
                    thumbnailUrl,
                    item.getQuantity()
            ));

            subtotal = subtotal.add(itemTotal);
            totalItems += item.getQuantity();
        }

        return new CartResponse(cart.getId(), itemResponses, subtotal, totalItems);
    }

    private String getThumbnailUrl(ProductVariant variant) {
        if (variant == null || variant.getImages() == null || variant.getImages().isEmpty()) return null;
        return variant.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(elin.le.giftly.product.entity.ProductImage::getSecureUrl)
                .findFirst()
                .orElseGet(() -> variant.getImages().stream()
                        .map(elin.le.giftly.product.entity.ProductImage::getSecureUrl)
                        .findFirst()
                        .orElse(null));
    }

    private UUID getCurrentUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }
}
