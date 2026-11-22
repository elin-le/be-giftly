package elin.le.giftly.product.service;

import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.dto.*;
import elin.le.giftly.product.entity.BundleComponent;
import elin.le.giftly.product.entity.BundleOption;
import elin.le.giftly.product.entity.BundleOptionGroup;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BundleOptionService {

    private final BundleOptionGroupRepository bundleOptionGroupRepository;
    private final BundleComponentRepository bundleComponentRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantPolicyRepository productVariantPolicyRepository;
    private final GiftBoxVariantDetailRepository giftBoxVariantDetailRepository;

    @Transactional(readOnly = true)
    public List<BundleOptionGroupResponse> getBundleOptionGroups(UUID variantId) {
        // Validate variant exists
        productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", variantId));

        List<BundleOptionGroup> groups = bundleOptionGroupRepository.findByBundleVariantId(variantId);
        return groups.stream()
                .map(this::toBundleOptionGroupResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BundleComponentResponse> getBundleComponents(UUID variantId) {
        // Validate variant exists
        productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", variantId));

        List<BundleComponent> components = bundleComponentRepository.findByBundleVariantId(variantId);
        return components.stream()
                .map(this::toBundleComponentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductVariantPolicyResponse getProductVariantPolicy(UUID variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", variantId));

        elin.le.giftly.product.entity.ProductVariantPolicy policy = productVariantPolicyRepository.findByVariantId(variantId);
        if (policy == null) {
            throw new ResourceNotFoundException("Product Variant Policy", "variantId", variantId);
        }

        return new ProductVariantPolicyResponse(
                policy.getId(),
                variant.getId(),
                policy.getIsPerishable(),
                policy.getShelfLifeHours(),
                policy.getReturnPolicy(),
                policy.getAdvancePaymentPercent()
        );
    }

    @Transactional(readOnly = true)
    public GiftBoxVariantDetailResponse getGiftBoxVariantDetail(UUID variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", variantId));

        elin.le.giftly.product.entity.GiftBoxVariantDetail detail = giftBoxVariantDetailRepository.findByVariantId(variantId);
        if (detail == null) {
            throw new ResourceNotFoundException("Gift Box Variant Detail", "variantId", variantId);
        }

        return new GiftBoxVariantDetailResponse(
                detail.getId(),
                variant.getId(),
                detail.getInnerLength(),
                detail.getInnerWidth(),
                detail.getInnerHeight(),
                detail.getMaxWeight(),
                detail.getDimensionUnit(),
                detail.getWeightUnit()
        );
    }

    private BundleOptionGroupResponse toBundleOptionGroupResponse(BundleOptionGroup group) {
        List<BundleOptionResponse> optionResponses = group.getOptions().stream()
                .map(this::toBundleOptionResponse)
                .collect(Collectors.toList());

        return new BundleOptionGroupResponse(
                group.getId(),
                group.getName(),
                group.getGroupType(),
                group.getSelectionType(),
                group.getMinSelections(),
                group.getMaxSelections(),
                group.getIsRequired(),
                group.getDisplayOrder(),
                optionResponses
        );
    }

    private BundleOptionResponse toBundleOptionResponse(BundleOption option) {
        return new BundleOptionResponse(
                option.getId(),
                option.getProductVariantId(),
                option.getPricingMode(),
                option.getFixedPrice()
        );
    }

    private BundleComponentResponse toBundleComponentResponse(BundleComponent component) {
        return new BundleComponentResponse(
                component.getId(),
                component.getComponentVariantId(),
                component.getComponentRole(),
                component.getQuantity(),
                component.getIsRequired()
        );
    }
}
