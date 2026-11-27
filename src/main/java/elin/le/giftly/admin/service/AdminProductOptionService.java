package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.CreateProductOptionRequest;
import elin.le.giftly.admin.dto.CreateProductOptionValueRequest;
import elin.le.giftly.admin.dto.UpdateProductOptionRequest;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductOption;
import elin.le.giftly.product.entity.ProductOptionValue;
import elin.le.giftly.product.repository.ProductOptionRepository;
import elin.le.giftly.product.repository.ProductOptionValueRepository;
import elin.le.giftly.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminProductOptionService {

    private static final Logger log = LoggerFactory.getLogger(AdminProductOptionService.class);

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionValueRepository productOptionValueRepository;

    /**
     * Create a new option group for a product (e.g., "Màu sắc" with values ["Đỏ", "Xanh"]).
     */
    @Transactional
    public ProductOption createOption(UUID productId, CreateProductOptionRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        ProductOption option = ProductOption.builder()
            .product(product)
            .name(request.name())
            .displayOrder(request.displayOrder())
            .values(new ArrayList<>())
            .build();

        // Create option values if provided
        if (request.values() != null && !request.values().isEmpty()) {
            for (CreateProductOptionValueRequest valueReq : request.values()) {
                ProductOptionValue value = ProductOptionValue.builder()
                    .option(option)
                    .value(valueReq.value())
                    .colorHex(valueReq.colorHex())
                    .priceAdjustment(valueReq.priceAdjustment())
                    .build();
                option.getValues().add(value);
            }
        }

        option = productOptionRepository.save(option);
        log.info("Created product option: id={}, name={}, product={}, values={}",
            option.getId(), option.getName(), productId,
            option.getValues() != null ? option.getValues().size() : 0);
        return option;
    }

    /**
     * Update an existing option group's name, display order, and/or values.
     * If values are provided, they replace all existing values.
     * Note: Replacing values will break variant-option links for deleted values.
     */
    @Transactional
    public ProductOption updateOption(UUID productId, UUID optionId, UpdateProductOptionRequest request) {
        ProductOption option = productOptionRepository.findById(optionId)
            .orElseThrow(() -> new ResourceNotFoundException("Product Option", "id", optionId));

        if (!option.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Product Option", "product", productId);
        }

        option.setName(request.name());
        if (request.displayOrder() != null) {
            option.setDisplayOrder(request.displayOrder());
        }

        // Replace values if provided
        if (request.values() != null) {
            // Find old value IDs that no longer exist in the new list
            List<String> newValueStrings = new ArrayList<>();
            if (request.values() != null) {
                for (CreateProductOptionValueRequest v : request.values()) {
                    newValueStrings.add(v.value());
                }
            }

            // Remove deleted option values — cascade REMOVE will delete variant-option links automatically
            List<ProductOptionValue> oldValues = new ArrayList<>(option.getValues());
            for (ProductOptionValue oldValue : oldValues) {
                if (!newValueStrings.contains(oldValue.getValue())) {
                    option.getValues().remove(oldValue);
                    productOptionValueRepository.delete(oldValue);
                } else {
                    // Update existing value if colorHex or priceAdjustment changed
                    CreateProductOptionValueRequest matchingReq = findMatchingRequest(request.values(), oldValue.getValue());
                    if (matchingReq != null) {
                        oldValue.setColorHex(matchingReq.colorHex());
                        if (matchingReq.priceAdjustment() != null) {
                            oldValue.setPriceAdjustment(matchingReq.priceAdjustment());
                        }
                    }
                }
            }

            // Add new values
            for (CreateProductOptionValueRequest valueReq : request.values()) {
                if (!newValueStringsAlreadyAdded(option, valueReq.value())) {
                    ProductOptionValue value = ProductOptionValue.builder()
                        .option(option)
                        .value(valueReq.value())
                        .colorHex(valueReq.colorHex())
                        .priceAdjustment(valueReq.priceAdjustment())
                        .build();
                    option.getValues().add(value);
                }
            }
        }

        return productOptionRepository.save(option);
    }

    /**
     * Add a new value to an existing option group (e.g., add "Xanh lá" to "Màu sắc").
     */
    @Transactional
    public ProductOptionValue addOptionValue(UUID productId, UUID optionId, CreateProductOptionValueRequest request) {
        ProductOption option = productOptionRepository.findById(optionId)
            .orElseThrow(() -> new ResourceNotFoundException("Product Option", "id", optionId));

        if (!option.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Product Option", "product", productId);
        }

        // Check for duplicate value
        for (ProductOptionValue existing : option.getValues()) {
            if (existing.getValue().equalsIgnoreCase(request.value())) {
                throw new IllegalArgumentException("Option value already exists: " + request.value());
            }
        }

        ProductOptionValue value = ProductOptionValue.builder()
            .option(option)
            .value(request.value())
            .colorHex(request.colorHex())
            .priceAdjustment(request.priceAdjustment())
            .build();

        option.getValues().add(value);
        productOptionValueRepository.save(value);
        log.info("Added option value: id={}, value={}, option={}", value.getId(), value.getValue(), optionId);
        return value;
    }

    /**
     * Update a specific option value (e.g., change "Đỏ" to "Đỏ tươi" or update colorHex).
     */
    @Transactional
    public ProductOptionValue updateOptionValue(UUID productId, UUID optionId, UUID valueId, CreateProductOptionValueRequest request) {
        ProductOptionValue value = productOptionValueRepository.findById(valueId)
            .orElseThrow(() -> new ResourceNotFoundException("Product Option Value", "id", valueId));

        if (!value.getOption().getId().equals(optionId)) {
            throw new ResourceNotFoundException("Product Option Value", "option", optionId);
        }

        if (!value.getOption().getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Product Option Value", "product", productId);
        }

        value.setValue(request.value());
        value.setColorHex(request.colorHex());
        if (request.priceAdjustment() != null) {
            value.setPriceAdjustment(request.priceAdjustment());
        }
        return productOptionValueRepository.save(value);
    }

    /**
     * Delete an option group and all its values.
     * Also removes variant-option links for all values of this option.
     */
    @Transactional
    public void deleteOption(UUID productId, UUID optionId) {
        ProductOption option = productOptionRepository.findById(optionId)
            .orElseThrow(() -> new ResourceNotFoundException("Product Option", "id", optionId));

        if (!option.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Product Option", "product", productId);
        }

        // Deleting the option cascades to values (orphanRemoval=true),
        // and deleting each value cascades to variant links (CascadeType.REMOVE)
        productOptionRepository.delete(option);
        log.info("Deleted product option: id={}, product={}", optionId, productId);
    }

    /**
     * Delete a specific option value from an option group.
     * Also removes variant-option links for this value.
     */
    @Transactional
    public void deleteOptionValue(UUID productId, UUID optionId, UUID valueId) {
        ProductOptionValue value = productOptionValueRepository.findById(valueId)
            .orElseThrow(() -> new ResourceNotFoundException("Product Option Value", "id", valueId));

        if (!value.getOption().getId().equals(optionId)) {
            throw new ResourceNotFoundException("Product Option Value", "option", optionId);
        }

        if (!value.getOption().getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Product Option Value", "product", productId);
        }

        // Removing from parent collection triggers orphanRemoval on the option,
        // and cascade REMOVE deletes variant-option links automatically
        value.getOption().getValues().remove(value);
        productOptionValueRepository.delete(value);
        log.info("Deleted option value: id={}, option={}", valueId, optionId);
    }

    // --- Private helpers ---

    private CreateProductOptionValueRequest findMatchingRequest(List<CreateProductOptionValueRequest> requests, String value) {
        if (requests == null) return null;
        for (CreateProductOptionValueRequest req : requests) {
            if (req.value().equals(value)) {
                return req;
            }
        }
        return null;
    }

    private boolean newValueStringsAlreadyAdded(ProductOption option, String value) {
        for (ProductOptionValue existing : option.getValues()) {
            if (existing.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
