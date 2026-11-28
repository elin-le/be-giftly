package elin.le.giftly.giftbox.service;

import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.CustomComponentType;
import elin.le.giftly.common.enums.VariantStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.giftbox.dto.CreateGiftBoxRequest;
import elin.le.giftly.giftbox.dto.GiftBoxComponentRequest;
import elin.le.giftly.giftbox.dto.GiftBoxItemRequest;
import elin.le.giftly.giftbox.dto.GiftBoxResponse;
import elin.le.giftly.giftbox.dto.SaveGiftBoxItemsRequest;
import elin.le.giftly.giftbox.dto.UpdateGiftBoxRequest;
import elin.le.giftly.giftbox.entity.GiftBoxConfig;
import elin.le.giftly.giftbox.entity.GiftBoxConfigItem;
import elin.le.giftly.giftbox.repository.GiftBoxConfigRepository;
import elin.le.giftly.giftbox.repository.GiftBoxConfigItemRepository;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.dto.ProductResponse;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GiftBoxService {

    private final GiftBoxConfigRepository giftBoxConfigRepository;
    private final GiftBoxConfigItemRepository giftBoxConfigItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final GiftBoxPurchaseValidator giftBoxPurchaseValidator;

    @Transactional
    public GiftBoxResponse createGiftBox(CreateGiftBoxRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);
        return createGiftBoxForUser(userId, request);
    }

    @Transactional
    public GiftBoxResponse createGiftBoxForUser(UUID userId, CreateGiftBoxRequest request) {
        GiftBoxConfig config = GiftBoxConfig.builder()
                .userId(userId)
                .build();
        config = giftBoxConfigRepository.save(config);
        replaceGiftBoxSelections(config, request.boxVariantId(),
                request.components() == null ? List.of() : request.components(), request.items());
        return toGiftBoxResponse(config);
    }

    @Transactional
    public GiftBoxResponse updateGiftBoxForAdmin(UUID giftBoxId, UpdateGiftBoxRequest request) {
        GiftBoxConfig config = giftBoxConfigRepository.findById(giftBoxId)
                .orElseThrow(() -> new ResourceNotFoundException("GiftBox", "id", giftBoxId));
        replaceGiftBoxSelections(config, request.boxVariantId(), request.components(), request.items());
        config.setReviewStatus(elin.le.giftly.common.enums.GiftBoxReviewStatus.PENDING);
        config.setReviewNote(null);
        giftBoxPurchaseValidator.validateForSave(giftBoxId, config.getUserId());
        return toGiftBoxResponse(config);
    }

    @Transactional(readOnly = true)
    public GiftBoxResponse getGiftBoxResponseForAdmin(UUID giftBoxId) {
        GiftBoxConfig config = giftBoxConfigRepository.findById(giftBoxId)
                .orElseThrow(() -> new ResourceNotFoundException("GiftBox", "id", giftBoxId));
        return toGiftBoxResponse(config);
    }

    @Transactional
    public GiftBoxResponse updateGiftBox(UUID giftBoxId, UpdateGiftBoxRequest request) {
        UUID userId = getCurrentUserId(SecurityContextHolder.getContext().getAuthentication().getName());
        GiftBoxConfig config = getOwnedConfig(giftBoxId, userId);
        replaceGiftBoxSelections(config, request.boxVariantId(), request.components(), request.items());
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

    @Transactional
    public GiftBoxResponse addGiftBoxItem(UUID giftBoxId, elin.le.giftly.giftbox.dto.GiftBoxItemRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        GiftBoxConfig config = giftBoxConfigRepository.findById(giftBoxId)
                .orElseThrow(() -> new ResourceNotFoundException("GiftBox", "id", giftBoxId));

        if (!config.getUserId().equals(userId)) {
            throw new BadRequestException("You do not have access to this gift box");
        }

        ProductVariant variant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", request.productVariantId()));

        if (variant.getProduct().getCustomComponentType() != null) {
            throw new BadRequestException("Custom components must be added through the components endpoint");
        }

        // Validate: no READY_MADE_GIFT_SET in custom box
        if (variant.getProduct().getProductType() == ProductType.READY_MADE_GIFT_SET) {
            throw new BadRequestException("Ready-made gift sets cannot be added to a custom box");
        }

        // Validate: REGULAR products must be custom box eligible
        if (variant.getProduct().getProductType() == ProductType.REGULAR
                && Boolean.FALSE.equals(variant.getProduct().getIsCustomBoxEligible())) {
            throw new BadRequestException("Product is not eligible for custom box: " + variant.getProduct().getName());
        }

        if (variant.getStatus() != VariantStatus.AVAILABLE) {
            throw new BadRequestException("Product variant is not available: " + variant.getSku());
        }

        GiftBoxConfigItem item = GiftBoxConfigItem.builder()
                .config(config)
                .productVariantId(variant.getId())
                .quantity(request.quantity())
                .build();
        giftBoxConfigItemRepository.save(item);
        config.getItems().add(item);
        giftBoxPurchaseValidator.validateForSave(giftBoxId, userId);

        return toGiftBoxResponse(config);
    }

    @Transactional
    public GiftBoxResponse saveGiftBoxItems(UUID giftBoxId, SaveGiftBoxItemsRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);
        GiftBoxConfig config = getOwnedConfig(giftBoxId, userId);

        List<GiftBoxConfigItem> existingItems = giftBoxConfigItemRepository.findByConfigId(giftBoxId);
        List<GiftBoxConfigItem> contentItems = existingItems.stream()
                .filter(item -> item.getComponentType() == null)
                .toList();
        giftBoxConfigItemRepository.deleteAll(contentItems);
        config.getItems().removeAll(contentItems);

        List<GiftBoxConfigItem> items = request.items().stream()
                .map(itemRequest -> toConfigItem(config, itemRequest))
                .toList();
        giftBoxConfigItemRepository.saveAll(items);
        config.getItems().addAll(items);

        giftBoxPurchaseValidator.validateForSave(giftBoxId, userId);
        return toGiftBoxResponse(config);
    }

    private GiftBoxConfigItem toConfigItem(GiftBoxConfig config, GiftBoxItemRequest request) {
        return GiftBoxConfigItem.builder()
                .config(config)
                .productVariantId(request.productVariantId())
                .quantity(request.quantity())
                .build();
    }

    private void replaceGiftBoxSelections(
            GiftBoxConfig config,
            UUID boxVariantId,
            List<GiftBoxComponentRequest> componentRequests,
            List<GiftBoxItemRequest> itemRequests) {
        ProductVariant boxVariant = getVariantAndValidateType(boxVariantId, ProductType.READY_MADE_GIFT_BOX);
        List<GiftBoxConfigItem> replacementItems = new ArrayList<>();
        replacementItems.add(GiftBoxConfigItem.builder()
                .config(config)
                .productVariantId(boxVariant.getId())
                .componentType(CustomComponentType.BOX)
                .quantity(1)
                .build());

        Set<CustomComponentType> selectedComponentTypes = new HashSet<>();
        for (GiftBoxComponentRequest componentRequest : componentRequests) {
            ProductVariant variant = productVariantRepository.findById(componentRequest.productVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product Variant", "id", componentRequest.productVariantId()));
            CustomComponentType componentType = validateCustomComponent(variant);
            if (!selectedComponentTypes.add(componentType)) {
                throw new BadRequestException("Only one component can be selected for type: " + componentType);
            }
            replacementItems.add(GiftBoxConfigItem.builder()
                    .config(config)
                    .productVariantId(variant.getId())
                    .componentType(componentType)
                    .quantity(1)
                    .build());
        }

        replacementItems.addAll(itemRequests.stream()
                .map(itemRequest -> toConfigItem(config, itemRequest))
                .toList());

        List<GiftBoxConfigItem> existingItems = giftBoxConfigItemRepository.findByConfigId(config.getId());
        if (!existingItems.isEmpty()) {
            giftBoxConfigItemRepository.deleteAll(existingItems);
            config.getItems().removeAll(existingItems);
            giftBoxConfigItemRepository.flush();
        }
        giftBoxConfigItemRepository.saveAll(replacementItems);
        config.getItems().addAll(replacementItems);
        giftBoxPurchaseValidator.validateForSave(config.getId(), config.getUserId());
    }

    @Transactional
    public GiftBoxResponse addGiftBoxComponent(UUID giftBoxId, GiftBoxComponentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);
        GiftBoxConfig config = getOwnedConfig(giftBoxId, userId);

        ProductVariant variant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", request.productVariantId()));
        CustomComponentType componentType = validateCustomComponent(variant);
        if (giftBoxConfigItemRepository.findByConfigId(giftBoxId).stream()
                .anyMatch(item -> item.getComponentType() == componentType)) {
            throw new BadRequestException("Only one component can be selected for type: " + componentType);
        }

        GiftBoxConfigItem component = GiftBoxConfigItem.builder()
                .config(config)
                .productVariantId(variant.getId())
                .componentType(componentType)
                .quantity(1)
                .build();
        giftBoxConfigItemRepository.save(component);
        config.getItems().add(component);
        giftBoxPurchaseValidator.validateForSave(giftBoxId, userId);
        return toGiftBoxResponse(config);
    }

    @Transactional
    public GiftBoxResponse removeGiftBoxComponent(UUID giftBoxId, String componentTypeName) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);
        GiftBoxConfig config = getOwnedConfig(giftBoxId, userId);

        CustomComponentType componentType;
        try {
            componentType = CustomComponentType.valueOf(componentTypeName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown custom component type: " + componentTypeName);
        }
        if (componentType == CustomComponentType.BOX) {
            throw new BadRequestException("The gift box cannot be removed from a custom box");
        }

        GiftBoxConfigItem component = giftBoxConfigItemRepository.findByConfigId(giftBoxId).stream()
                .filter(item -> item.getComponentType() == componentType)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("GiftBoxComponent", "type", componentType));
        giftBoxConfigItemRepository.delete(component);
        config.getItems().remove(component);
        return toGiftBoxResponse(config);
    }

    @Transactional
    public GiftBoxResponse removeGiftBoxItem(UUID giftBoxId, UUID itemId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = getCurrentUserId(email);

        GiftBoxConfig config = giftBoxConfigRepository.findById(giftBoxId)
                .orElseThrow(() -> new ResourceNotFoundException("GiftBox", "id", giftBoxId));

        if (!config.getUserId().equals(userId)) {
            throw new BadRequestException("You do not have access to this gift box");
        }

        GiftBoxConfigItem item = giftBoxConfigItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("GiftBoxConfigItem", "id", itemId));

        if (!item.getConfig().getId().equals(giftBoxId)) {
            throw new BadRequestException("Item does not belong to this gift box");
        }
        if (item.getComponentType() != null) {
            throw new BadRequestException("Custom components must be removed through the components endpoint");
        }

        giftBoxConfigItemRepository.delete(item);
        config.getItems().remove(item);

        return toGiftBoxResponse(config);
    }

    private GiftBoxResponse toGiftBoxResponse(GiftBoxConfig config) {
        List<GiftBoxConfigItem> items = giftBoxConfigItemRepository.findByConfigId(config.getId());

        BigDecimal totalPrice = BigDecimal.ZERO;
        GiftBoxResponse.BoxProductResponse boxResponse = null;
        List<GiftBoxResponse.ComponentResponse> componentResponses = new ArrayList<>();
        List<GiftBoxResponse.GiftBoxItemResponse> itemResponses = new ArrayList<>();
        for (GiftBoxConfigItem item : items) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", item.getProductVariantId()));
            Product product = variant.getProduct();
            BigDecimal price = item.getComponentType() == null
                    && product.getProductType() == ProductType.PACKAGING_ACCESSORY
                    ? BigDecimal.ZERO : variant.getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(subtotal);
            List<ProductResponse.ImageResponse> images = getImageResponses(product, variant);

            if (item.getComponentType() == CustomComponentType.BOX) {
                boxResponse = new GiftBoxResponse.BoxProductResponse(
                        variant.getId(), product.getName(), variant.getSku(), price, images);
            } else if (item.getComponentType() != null) {
                componentResponses.add(new GiftBoxResponse.ComponentResponse(
                        variant.getId(), item.getComponentType().name(), product.getName(),
                        variant.getSku(), price, images));
            } else {
                itemResponses.add(new GiftBoxResponse.GiftBoxItemResponse(
                        item.getId(), variant.getId(), product.getName(), variant.getSku(),
                        price, item.getQuantity(), subtotal, images));
            }
        }

        if (boxResponse == null) {
            throw new ResourceNotFoundException("GiftBoxComponent", "type", CustomComponentType.BOX);
        }

        return new GiftBoxResponse(
                config.getId(),
                boxResponse,
                componentResponses,
                itemResponses,
                totalPrice
        );
    }

    private List<ProductResponse.ImageResponse> getImageResponses(Product product, ProductVariant variant) {
        List<ProductImage> images = product.getCommonImages() != null && !product.getCommonImages().isEmpty()
                ? product.getCommonImages() : variant.getImages();
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .map(image -> new ProductResponse.ImageResponse(
                        image.getId(), image.getImageUrl(), image.getPublicId(), image.getSecureUrl(),
                        image.getIsPrimary(), image.getDisplayOrder()))
                .toList();
    }

    private CustomComponentType validateCustomComponent(ProductVariant variant) {
        if (variant.getStatus() != VariantStatus.AVAILABLE) {
            throw new BadRequestException("Product variant is not available: " + variant.getSku());
        }
        CustomComponentType componentType = variant.getProduct().getCustomComponentType();
        if (componentType == null || componentType == CustomComponentType.BOX) {
            throw new BadRequestException("Product is not a selectable custom component: " + variant.getProduct().getName());
        }
        return componentType;
    }

    private GiftBoxConfig getOwnedConfig(UUID giftBoxId, UUID userId) {
        GiftBoxConfig config = giftBoxConfigRepository.findById(giftBoxId)
                .orElseThrow(() -> new ResourceNotFoundException("GiftBox", "id", giftBoxId));
        if (!config.getUserId().equals(userId)) {
            throw new BadRequestException("You do not have access to this gift box");
        }
        return config;
    }

    private ProductVariant getVariantAndValidateType(UUID variantId, ProductType expectedType) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", variantId));

        if (variant.getStatus() != VariantStatus.AVAILABLE) {
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
