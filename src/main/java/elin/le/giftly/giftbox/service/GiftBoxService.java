package elin.le.giftly.giftbox.service;

import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.giftbox.dto.CreateGiftBoxRequest;
import elin.le.giftly.giftbox.dto.GiftBoxResponse;
import elin.le.giftly.giftbox.entity.GiftBoxConfig;
import elin.le.giftly.giftbox.entity.GiftBoxItem;
import elin.le.giftly.giftbox.repository.GiftBoxConfigRepository;
import elin.le.giftly.giftbox.repository.GiftBoxItemRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GiftBoxService {

    private final GiftBoxConfigRepository giftBoxConfigRepository;
    private final GiftBoxItemRepository giftBoxItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public GiftBoxResponse createGiftBox(CreateGiftBoxRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        // Validate box product
        Product boxProduct = getProductAndValidateType(request.boxProductId(), ProductType.GIFT_BOX);

        // Validate optional products
        Product wrappingProduct = null;
        if (request.wrappingProductId() != null) {
            wrappingProduct = getProductAndValidateType(request.wrappingProductId(), ProductType.WRAPPING);
        }

        Product ribbonProduct = null;
        if (request.ribbonProductId() != null) {
            ribbonProduct = getProductAndValidateType(request.ribbonProductId(), ProductType.RIBBON);
        }

        Product cardProduct = null;
        if (request.cardProductId() != null) {
            cardProduct = getProductAndValidateType(request.cardProductId(), ProductType.GREETING_CARD);
        }

        // Create gift box config
        GiftBoxConfig config = GiftBoxConfig.builder()
                .userId(userId)
                .boxProduct(boxProduct)
                .wrappingProduct(wrappingProduct)
                .ribbonProduct(ribbonProduct)
                .cardProduct(cardProduct)
                .greetingMessage(request.greetingMessage())
                .build();

        config = giftBoxConfigRepository.save(config);

        // Create gift box items
        List<GiftBoxItem> items = new ArrayList<>();
        for (var itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.productId()));

            if (product.getStatus() != ProductStatus.AVAILABLE) {
                throw new BadRequestException("Product is not available: " + product.getName());
            }

            GiftBoxItem item = GiftBoxItem.builder()
                    .config(config)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .build();
            items.add(item);
        }
        giftBoxItemRepository.saveAll(items);
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
        List<GiftBoxItem> items = giftBoxItemRepository.findByConfigId(config.getId());

        List<GiftBoxResponse.GiftBoxItemResponse> itemResponses = items.stream()
                .map(item -> {
                    Product product = item.getProduct();
                    BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new GiftBoxResponse.GiftBoxItemResponse(
                            item.getId(),
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            item.getQuantity(),
                            subtotal
                    );
                }).toList();

        // Calculate total price
        BigDecimal totalPrice = config.getBoxProduct().getPrice();

        if (config.getWrappingProduct() != null) {
            totalPrice = totalPrice.add(config.getWrappingProduct().getPrice());
        }
        if (config.getRibbonProduct() != null) {
            totalPrice = totalPrice.add(config.getRibbonProduct().getPrice());
        }
        if (config.getCardProduct() != null) {
            totalPrice = totalPrice.add(config.getCardProduct().getPrice());
        }
        for (GiftBoxItem item : items) {
            totalPrice = totalPrice.add(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        GiftBoxResponse.BoxProductResponse boxResponse = new GiftBoxResponse.BoxProductResponse(
                config.getBoxProduct().getId(),
                config.getBoxProduct().getName(),
                config.getBoxProduct().getPrice(),
                getThumbnailUrl(config.getBoxProduct())
        );

        GiftBoxResponse.ProductResponse wrappingResponse = toProductResponse(config.getWrappingProduct());
        GiftBoxResponse.ProductResponse ribbonResponse = toProductResponse(config.getRibbonProduct());
        GiftBoxResponse.ProductResponse cardResponse = toProductResponse(config.getCardProduct());

        return new GiftBoxResponse(
                config.getId(),
                boxResponse,
                wrappingResponse,
                ribbonResponse,
                cardResponse,
                config.getGreetingMessage(),
                itemResponses,
                totalPrice
        );
    }

    private GiftBoxResponse.ProductResponse toProductResponse(Product product) {
        if (product == null) return null;
        return new GiftBoxResponse.ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                getThumbnailUrl(product)
        );
    }

    private String getThumbnailUrl(Product product) {
        if (product == null || product.getImages() == null) return null;
        return product.getImages().stream()
                .filter(ProductImage::getIsThumbnail)
                .map(ProductImage::getSecureUrl)
                .findFirst()
                .orElseGet(() -> product.getImages().stream()
                        .map(ProductImage::getSecureUrl)
                        .findFirst()
                        .orElse(null));
    }

    private Product getProductAndValidateType(UUID productId, ProductType expectedType) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new BadRequestException("Product is not available: " + product.getName());
        }

        if (product.getProductType() != expectedType) {
            throw new BadRequestException(
                    "Product type mismatch: expected " + expectedType + ", got " + product.getProductType()
            );
        }

        return product;
    }

    private UUID getCurrentUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }
}
