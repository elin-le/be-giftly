package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.CreateBundleOptionGroupRequest;
import elin.le.giftly.admin.dto.CreateBundleOptionRequest;
import elin.le.giftly.admin.dto.UpdateBundleComponentRequest;
import elin.le.giftly.admin.dto.UpdateBundleOptionGroupRequest;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.dto.BundleOptionGroupResponse;
import elin.le.giftly.product.dto.BundleOptionResponse;
import elin.le.giftly.product.entity.BundleComponent;
import elin.le.giftly.product.entity.BundleOption;
import elin.le.giftly.product.entity.BundleOptionGroup;
import elin.le.giftly.product.entity.ProductVariant;
import elin.le.giftly.product.repository.BundleComponentRepository;
import elin.le.giftly.product.repository.BundleOptionGroupRepository;
import elin.le.giftly.product.repository.BundleOptionRepository;
import elin.le.giftly.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBundleOptionService {

    private final BundleOptionGroupRepository bundleOptionGroupRepository;
    private final BundleOptionRepository bundleOptionRepository;
    private final BundleComponentRepository bundleComponentRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public BundleOptionGroupResponse createBundleOptionGroup(CreateBundleOptionGroupRequest request) {
        // Validate variant exists
        productVariantRepository.findById(request.bundleVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", request.bundleVariantId()));

        BundleOptionGroup group = BundleOptionGroup.builder()
                .bundleVariantId(request.bundleVariantId())
                .name(request.name())
                .groupType(request.groupType())
                .selectionType(request.selectionType())
                .minSelections(request.minSelections())
                .maxSelections(request.maxSelections())
                .isRequired(request.isRequired())
                .displayOrder(request.displayOrder())
                .options(new ArrayList<>())
                .build();

        // Build options if provided
        if (request.options() != null && !request.options().isEmpty()) {
            for (CreateBundleOptionRequest optReq : request.options()) {
                BundleOption option = BundleOption.builder()
                        .optionGroup(group)
                        .productVariantId(optReq.productVariantId())
                        .pricingMode(optReq.pricingMode())
                        .fixedPrice(optReq.fixedPrice())
                        .build();
                group.getOptions().add(option);
            }
        }

        group = bundleOptionGroupRepository.save(group);
        return toBundleOptionGroupResponse(group);
    }

    @Transactional
    public BundleOptionGroupResponse updateBundleOptionGroup(UUID groupId, UpdateBundleOptionGroupRequest request) {
        BundleOptionGroup group = bundleOptionGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle Option Group", "id", groupId));

        group.setName(request.name());
        group.setGroupType(request.groupType());
        group.setSelectionType(request.selectionType());
        group.setMinSelections(request.minSelections());
        group.setMaxSelections(request.maxSelections());
        group.setIsRequired(request.isRequired());
        group.setDisplayOrder(request.displayOrder());

        // Replace options if provided
        if (request.options() != null) {
            group.getOptions().clear();
            for (CreateBundleOptionRequest optReq : request.options()) {
                BundleOption option = BundleOption.builder()
                        .optionGroup(group)
                        .productVariantId(optReq.productVariantId())
                        .pricingMode(optReq.pricingMode())
                        .fixedPrice(optReq.fixedPrice())
                        .build();
                group.getOptions().add(option);
            }
        }

        group = bundleOptionGroupRepository.save(group);
        return toBundleOptionGroupResponse(group);
    }

    @Transactional
    public void deleteBundleOptionGroup(UUID groupId) {
        BundleOptionGroup group = bundleOptionGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle Option Group", "id", groupId));
        bundleOptionGroupRepository.delete(group);
    }

    @Transactional
    public void deleteBundleOption(UUID optionId) {
        BundleOption option = bundleOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle Option", "id", optionId));
        bundleOptionRepository.delete(option);
    }

    // Bundle Component CRUD
    @Transactional
    public elin.le.giftly.product.dto.BundleComponentResponse createBundleComponent(
            elin.le.giftly.admin.dto.CreateBundleComponentRequest request) {
        // Validate variant exists
        productVariantRepository.findById(request.bundleVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", request.bundleVariantId()));
        productVariantRepository.findById(request.componentVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Component Variant", "id", request.componentVariantId()));

        BundleComponent component = BundleComponent.builder()
                .bundleVariantId(request.bundleVariantId())
                .componentVariantId(request.componentVariantId())
                .componentRole(request.componentRole())
                .quantity(request.quantity())
                .isRequired(request.isRequired())
                .build();

        component = bundleComponentRepository.save(component);
        return toBundleComponentResponse(component);
    }

    @Transactional
    public void deleteBundleComponent(UUID componentId) {
        BundleComponent component = bundleComponentRepository.findById(componentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle Component", "id", componentId));
        bundleComponentRepository.delete(component);
    }

    @Transactional
    public elin.le.giftly.product.dto.BundleComponentResponse updateBundleComponent(
            UUID componentId, UpdateBundleComponentRequest request) {
        BundleComponent component = bundleComponentRepository.findById(componentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle Component", "id", componentId));

        if (request.role() != null) component.setComponentRole(request.role());
        if (request.quantity() != null) component.setQuantity(request.quantity());

        component = bundleComponentRepository.save(component);
        return toBundleComponentResponse(component);
    }

    // List endpoints for admin
    @Transactional(readOnly = true)
    public List<BundleOptionGroupResponse> getAllBundleOptionGroups() {
        return bundleOptionGroupRepository.findAll().stream()
                .map(this::toBundleOptionGroupResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BundleOptionGroupResponse> getBundleOptionGroupsByVariantId(UUID variantId) {
        return bundleOptionGroupRepository.findByBundleVariantId(variantId).stream()
                .map(this::toBundleOptionGroupResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<elin.le.giftly.product.dto.BundleComponentResponse> getAllBundleComponents() {
        return bundleComponentRepository.findAll().stream()
                .map(this::toBundleComponentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<elin.le.giftly.product.dto.BundleComponentResponse> getBundleComponentsByVariantId(UUID variantId) {
        return bundleComponentRepository.findByBundleVariantId(variantId).stream()
                .map(this::toBundleComponentResponse)
                .collect(Collectors.toList());
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

    private elin.le.giftly.product.dto.BundleComponentResponse toBundleComponentResponse(BundleComponent component) {
        return new elin.le.giftly.product.dto.BundleComponentResponse(
                component.getId(),
                component.getComponentVariantId(),
                component.getComponentRole(),
                component.getQuantity(),
                component.getIsRequired()
        );
    }
}
