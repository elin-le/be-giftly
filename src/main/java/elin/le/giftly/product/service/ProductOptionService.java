package elin.le.giftly.product.service;

import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.dto.ProductOptionResponse;
import elin.le.giftly.product.dto.ProductOptionValueResponse;
import elin.le.giftly.product.dto.ProductResponse;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductOption;
import elin.le.giftly.product.entity.ProductOptionValue;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.ProductOptionRepository;
import elin.le.giftly.product.repository.ProductOptionValueRepository;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.product.repository.ProductVariantOptionValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductOptionService {

    private final ProductOptionRepository productOptionRepository;
    private final ProductRepository productRepository;
    private final ProductVariantOptionValueRepository productVariantOptionValueRepository;
    private final ProductOptionValueRepository productOptionValueRepository;

    @Transactional(readOnly = true)
    public List<ProductOptionResponse> getProductOptions(UUID productId) {
        // Validate product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        List<ProductOption> options = productOptionRepository.findByProductId(productId);
        return options.stream()
                .map(this::toProductOptionResponse)
                .collect(Collectors.toList());
    }

    private ProductOptionResponse toProductOptionResponse(ProductOption option) {
        List<ProductOptionValueResponse> valueResponses = option.getValues().stream()
                .map(this::toProductOptionValueResponse)
                .collect(Collectors.toList());

        return new ProductOptionResponse(
                option.getId(),
                option.getName(),
                option.getDisplayOrder(),
                valueResponses
        );
    }

    private ProductOptionValueResponse toProductOptionValueResponse(ProductOptionValue value) {
        return new ProductOptionValueResponse(
                value.getId(),
                value.getValue(),
                value.getColorHex(),
                value.getPriceAdjustment()
        );
    }

    /**
     * Resolve the variant that matches the given option value IDs for a product.
     * Compares the set of option value IDs attached to each variant with the requested set.
     */
    @Transactional(readOnly = true)
    public ProductResponse.VariantResponse resolveVariant(UUID productId, List<UUID> optionValueIds) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        List<ProductVariant> variants = product.getVariants();
        if (variants == null || variants.isEmpty()) {
            throw new ResourceNotFoundException("Product Variant", "product", productId);
        }

        Set<UUID> requestedSet = new HashSet<>(optionValueIds);

        for (ProductVariant variant : variants) {
            if (variant.getOptionValues() == null || variant.getOptionValues().isEmpty()) {
                continue;
            }

            Set<UUID> variantOptionValueIds = variant.getOptionValues().stream()
                    .map(pov -> pov.getOptionValue().getId())
                    .collect(Collectors.toSet());

            if (variantOptionValueIds.equals(requestedSet)) {
                return toVariantResponse(variant);
            }
        }

        throw new ResourceNotFoundException("Product Variant", "optionValues", optionValueIds.toString());
    }

    private ProductResponse.VariantResponse toVariantResponse(ProductVariant variant) {
        List<ProductResponse.ImageResponse> images = variant.getImages() != null
                ? variant.getImages().stream().map(img -> new ProductResponse.ImageResponse(
                        img.getId(), img.getImageUrl(), img.getPublicId(), img.getSecureUrl(),
                        img.getIsPrimary(), img.getDisplayOrder()
                )).collect(Collectors.toList())
                : List.of();

        List<ProductResponse.OptionValueInfo> optionValues = variant.getOptionValues() != null
                ? variant.getOptionValues().stream().map(pov -> new ProductResponse.OptionValueInfo(
                        pov.getOptionValue().getId(),
                        pov.getOptionValue().getOption().getName(),
                        pov.getOptionValue().getValue(),
                        pov.getOptionValue().getColorHex()
                )).collect(Collectors.toList())
                : List.of();

        // Calculate final price: base price + sum of option value price adjustments
        BigDecimal finalPrice = calculateFinalPrice(variant);

        return new ProductResponse.VariantResponse(
                variant.getId(),
                variant.getSku(),
                variant.getPrice(),
                finalPrice,
                variant.getStockQuantity(),
                variant.getLength(),
                variant.getWidth(),
                variant.getHeight(),
                variant.getDimensionUnit(),
                variant.getWeight(),
                variant.getWeightUnit(),
                variant.getStatus() != null ? variant.getStatus().name() : null,
                images,
                optionValues
        );
    }

    private BigDecimal calculateFinalPrice(ProductVariant variant) {
        if (variant.getOptionValues() == null || variant.getOptionValues().isEmpty()) {
            return variant.getPrice();
        }
        BigDecimal adjustments = variant.getOptionValues().stream()
                .map(pov -> pov.getOptionValue().getPriceAdjustment())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return variant.getPrice().add(adjustments);
    }
}
