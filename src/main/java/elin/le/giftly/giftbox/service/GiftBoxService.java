package elin.le.giftly.giftbox.service;

import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.giftbox.dto.CreateGiftBoxRequest;
import elin.le.giftly.giftbox.dto.GiftBoxResponse;
import elin.le.giftly.giftbox.entity.GiftBoxConfig;
import elin.le.giftly.giftbox.entity.GiftBoxConfigItem;
import elin.le.giftly.giftbox.repository.GiftBoxConfigRepository;
import elin.le.giftly.giftbox.repository.GiftBoxConfigItemRepository;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.ProductRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GiftBoxService {

    private final GiftBoxConfigRepository giftBoxConfigRepository;
    private final GiftBoxConfigItemRepository giftBoxConfigItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Transactional
    public GiftBoxResponse createGiftBox(CreateGiftBoxRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        // Validate box variant (must be READY_MADE_GIFT_BOX)
        ProductVariant boxVariant = getVariantAndValidateType(request.boxVariantId(), ProductType.READY_MADE_GIFT_BOX);

        // Create gift box config
        GiftBoxConfig config = GiftBoxConfig.builder()
                .userId(userId)
                .boxVariantId(boxVariant.getId())
                .build();

        config = giftBoxConfigRepository.save(config);

        // Create gift box items
        List<GiftBoxConfigItem> items = new ArrayList<>();
        for (var itemRequest : request.items()) {
            ProductVariant variant = productVariantRepository.findById(itemRequest.productVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", itemRequest.productVariantId()));

            // Validate: no READY_MADE_GIFT_SET in custom box
            if (variant.getProduct().getProductType() == ProductType.READY_MADE_GIFT_SET) {
                throw new BadRequestException("Ready-made gift sets cannot be added to a custom box");
            }

            // Validate: REGULAR products must be custom box eligible
            if (variant.getProduct().getProductType() == ProductType.REGULAR
                    && Boolean.FALSE.equals(variant.getProduct().getIsCustomBoxEligible())) {
                throw new BadRequestException("Product is not eligible for custom box: " + variant.getProduct().getName());
            }

            if (variant.getStatus() != ProductStatus.AVAILABLE) {
                throw new BadRequestException("Product variant is not available: " + variant.getSku());
            }

            GiftBoxConfigItem item = GiftBoxConfigItem.builder()
                    .config(config)
                    .productVariantId(variant.getId())
                    .quantity(itemRequest.quantity())
                    .build();
            items.add(item);
        }
        giftBoxConfigItemRepository.saveAll(items);
        config.getItems().addAll(items);

        return toGiftBoxResponse(config);
    }

    @Transactional(readOnly = true)
    public List<GiftBoxResponse> getMyGiftBoxes() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        List<GiftBoxConfig> configs = giftBoxConfigRepository.findByUserId(userId);
        return configs.stream()
                .map(this::toGiftBoxResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GiftBoxResponse getGiftBoxById(UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        GiftBoxConfig config = giftBoxConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GiftBox", "id", id));

        if (!config.getUserId().equals(userId)) {
            throw new BadRequestException("You do not have access to this gift box");
        }

        return toGiftBoxResponse(config);
    }

    @Transactional
    public void deleteGiftBox(UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        GiftBoxConfig config = giftBoxConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GiftBox", "id", id));

        if (!config.getUserId().equals(userId)) {
            throw new BadRequestException("You do not have access to this gift box");
        }

        giftBoxConfigRepository.delete(config);
    }

    private GiftBoxResponse toGiftBoxResponse(GiftBoxConfig config) {
        List<GiftBoxConfigItem> items = giftBoxConfigItemRepository.findByConfigId(config.getId());

        List<GiftBoxResponse.GiftBoxItemResponse> itemResponses = items.stream()
                .map(item -> {
                    ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", item.getProductVariantId()));
                    Product product = variant.getProduct();

                    // PACKAGING_ACCESSORY is free in custom box context
                    BigDecimal price = product.getProductType() == ProductType.PACKAGING_ACCESSORY
                            ? BigDecimal.ZERO
                            : variant.getPrice();

                    BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new GiftBoxResponse.GiftBoxItemResponse(
                            item.getId(),
                            variant.getId(),
                            product.getName(),
                            variant.getSku(),
                            price,
                            item.getQuantity(),
                            subtotal
                    );
                }).toList();

        // Calculate total price
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (GiftBoxConfigItem item : items) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", item.getProductVariantId()));
            Product product = variant.getProduct();

            BigDecimal price = product.getProductType() == ProductType.PACKAGING_ACCESSORY
                    ? BigDecimal.ZERO
                    : variant.getPrice();
            totalPrice = totalPrice.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // Add box price
        ProductVariant boxVariant = productVariantRepository.findById(config.getBoxVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", config.getBoxVariantId()));
        totalPrice = totalPrice.add(boxVariant.getPrice());

        GiftBoxResponse.BoxProductResponse boxResponse = new GiftBoxResponse.BoxProductResponse(
                boxVariant.getId(),
                boxVariant.getProduct().getName(),
                boxVariant.getPrice()
        );

        return new GiftBoxResponse(
                config.getId(),
                boxResponse,
                itemResponses,
                totalPrice
        );
    }

    private ProductVariant getVariantAndValidateType(UUID variantId, ProductType expectedType) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", variantId));

        if (variant.getStatus() != ProductStatus.AVAILABLE) {
            throw new BadRequestException("Product variant is not available: " + variant.getSku());
        }

        if (variant.getProduct().getProductType() != expectedType) {
            throw new BadRequestException(
                    "Product type mismatch: expected " + expectedType + ", got " + variant.getProduct().getProductType()
            );
        }

        return variant;
    }

    private UUID getCurrentUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }
}
