package elin.le.giftly.cart.controller;

import elin.le.giftly.cart.dto.AddCartItemRequest;
import elin.le.giftly.cart.dto.CartResponse;
import elin.le.giftly.cart.dto.UpdateCartItemQuantityRequest;
import elin.le.giftly.cart.service.CartService;
import elin.le.giftly.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Shopping cart management APIs for customers")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get shopping cart", description = "Retrieve the current user's shopping cart with all items")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        CartResponse cart = cartService.getCart();
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add a product to the current user's shopping cart")
    public ResponseEntity<ApiResponse<CartResponse>> addCartItem(
            @Parameter(description = "Cart item with productId and quantity", required = true)
            @Valid @RequestBody AddCartItemRequest request) {
        CartResponse cart = cartService.addCartItem(request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity", description = "Update the quantity of a specific item in the cart")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItemQuantity(
            @Parameter(description = "Cart item ID", required = true)
            @PathVariable UUID itemId,
            @Parameter(description = "New quantity for the cart item", required = true)
            @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        CartResponse cart = cartService.updateCartItemQuantity(itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Remove a specific item from the shopping cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @Parameter(description = "Cart item ID to remove", required = true)
            @PathVariable UUID itemId) {
        CartResponse cart = cartService.removeCartItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    @Operation(summary = "Clear shopping cart", description = "Remove all items from the current user's shopping cart")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
