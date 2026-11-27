package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.*;
import elin.le.giftly.cloudinary.service.CloudinaryService;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.CustomComponentType;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.*;
import elin.le.giftly.product.repository.CategoryRepository;
import elin.le.giftly.product.repository.GiftBoxVariantDetailRepository;
import elin.le.giftly.product.repository.ProductImageRepository;
import elin.le.giftly.product.repository.ProductOptionValueRepository;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.product.repository.ProductVariantPolicyRepository;
import elin.le.giftly.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private static final Logger log = LoggerFactory.getLogger(AdminProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantPolicyRepository productVariantPolicyRepository;
    private final GiftBoxVariantDetailRepository giftBoxVariantDetailRepository;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(UUID categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByStatus(ProductStatus status, Pageable pageable) {
        return productRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByType(ProductType type, Pageable pageable) {
        return productRepository.findByProductType(type, pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductById(UUID productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    @Transactional
    public Product createProduct(CreateProductRequest request) {
        log.info("Creating product: name={}, variants={}", request.name(), 
            request.variants() == null ? "null" : request.variants().size());
        if (request.variants() != null) {
            for (int i = 0; i < request.variants().size(); i++) {
                CreateVariantRequest vr = request.variants().get(i);
                log.info("  Variant[{}]: sku={}, price={}, stock={}", i, vr.sku(), vr.price(), vr.stockQuantity());
            }
        }
        
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));

        Product product = Product.builder()
            .category(category)
            .name(request.name())
            .description(request.description())
            .productType(request.productType())
            .customComponentType(resolveCustomComponentType(request.productType(), request.customComponentType()))
            .status(request.status())
            .isCustomBoxEligible(request.isCustomBoxEligible())
            .variants(new ArrayList<>())
            .commonImages(new ArrayList<>())
            .build();

        // Build common images if provided
        if (request.commonImages() != null && !request.commonImages().isEmpty()) {
            for (VariantImageRequest imgReq : request.commonImages()) {
                ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageUrl(imgReq.imageUrl())
                    .secureUrl(imgReq.secureUrl())
                    .publicId(imgReq.publicId())
                    .isPrimary(imgReq.isPrimary())
                    .displayOrder(imgReq.displayOrder())
                    .build();
                product.getCommonImages().add(image);
            }
        }

        // Build variants and add to product BEFORE saving
        if (request.variants() != null && !request.variants().isEmpty()) {
            for (CreateVariantRequest variantReq : request.variants()) {
                ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .sku(variantReq.sku())
                    .price(variantReq.price())
                    .stockQuantity(variantReq.stockQuantity())
                    .length(variantReq.length())
                    .width(variantReq.width())
                    .height(variantReq.height())
                    .dimensionUnit(variantReq.dimensionUnit())
                    .weight(variantReq.weight())
                    .weightUnit(variantReq.weightUnit())
                    .status(variantReq.status())
                    .optionValues(new ArrayList<>())
                    .images(new ArrayList<>())
                    .build();

                // Link option values
                if (variantReq.optionValueIds() != null) {
                    List<ProductOptionValue> optionValues = productOptionValueRepository.findAllById(variantReq.optionValueIds());
                    for (ProductOptionValue pov : optionValues) {
                        ProductVariantOptionValue povLink = ProductVariantOptionValue.builder()
                            .variant(variant)
                            .optionValue(pov)
                            .build();
                        variant.getOptionValues().add(povLink);
                    }
                }

                // Create gift box detail if provided
                if (variantReq.giftBoxDetail() != null) {
                    CreateGiftBoxDetailRequest gbReq = variantReq.giftBoxDetail();
                    GiftBoxVariantDetail giftBoxDetail = GiftBoxVariantDetail.builder()
                        .variant(variant)
                        .innerLength(gbReq.innerLength())
                        .innerWidth(gbReq.innerWidth())
                        .innerHeight(gbReq.innerHeight())
                        .maxWeight(gbReq.maxWeight())
                        .dimensionUnit(gbReq.dimensionUnit())
                        .weightUnit(gbReq.weightUnit())
                        .build();
                    variant.setGiftBoxVariantDetail(giftBoxDetail);
                }

                // Create variant policy if provided
                if (variantReq.variantPolicy() != null) {
                    CreateVariantPolicyRequest policyReq = variantReq.variantPolicy();
                    ProductVariantPolicy policy = ProductVariantPolicy.builder()
                        .variant(variant)
                        .isPerishable(policyReq.isPerishable())
                        .shelfLifeHours(policyReq.shelfLifeHours())
                        .returnPolicy(policyReq.returnPolicy())
                        .advancePaymentPercent(policyReq.advancePaymentPercent())
                        .build();
                    variant.setVariantPolicy(policy);
                }

                // Create images if provided
                if (variantReq.images() != null) {
                    for (VariantImageRequest imgReq : variantReq.images()) {
                        ProductImage image = ProductImage.builder()
                            .productVariant(variant)
                            .imageUrl(imgReq.imageUrl())
                            .secureUrl(imgReq.secureUrl())
                            .publicId(imgReq.publicId())
                            .isPrimary(imgReq.isPrimary())
                            .displayOrder(imgReq.displayOrder())
                            .build();
                        variant.getImages().add(image);
                    }
                }

                product.getVariants().add(variant);
            }
        }

        // Save product with all variants in one transaction
        log.info("Saving product with {} variants in memory", product.getVariants().size());
        Product saved = productRepository.save(product);
        log.info("Product saved! ID={}, variants in memory={}", saved.getId(), saved.getVariants().size());
        
        // Verify variants were saved to DB
        int savedCount = saved.getVariants().size();
        log.info("After save: product ID={}, variants count={}", saved.getId(), savedCount);
        for (int i = 0; i < savedCount; i++) {
            var v = saved.getVariants().get(i);
            log.info("  Saved variant[{}]: id={}, sku={}", i, v.getId(), v.getSku());
        }
        
        return saved;
    }

    @Transactional
    public Product updateProduct(UUID productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));
            product.setCategory(category);
        }
        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.productType() != null) {
            product.setProductType(request.productType());
        }
        if (request.customComponentType() != null || request.productType() != null) {
            product.setCustomComponentType(resolveCustomComponentType(
                    product.getProductType(), request.customComponentType()));
        }
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        if (request.isCustomBoxEligible() != null) {
            product.setIsCustomBoxEligible(request.isCustomBoxEligible());
        }

        // Update common images if provided
        if (request.commonImages() != null) {
            // Delete old common images that are no longer in the request
            deleteRemovedImages(
                product.getCommonImages(),
                request.commonImages()
            );
            product.getCommonImages().clear();
            for (VariantImageRequest imgReq : request.commonImages()) {
                ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageUrl(imgReq.imageUrl())
                    .secureUrl(imgReq.secureUrl())
                    .publicId(imgReq.publicId())
                    .isPrimary(imgReq.isPrimary())
                    .displayOrder(imgReq.displayOrder())
                    .build();
                product.getCommonImages().add(image);
            }
        }

        // Update or add variants if provided
        if (request.variants() != null && !request.variants().isEmpty()) {
            for (UpdateVariantRequest variantReq : request.variants()) {
                ProductVariant variant;

                // If id is null, create a new variant for this product
                if (variantReq.id() == null) {
                    // Check if a variant with this SKU already exists on this product
                    Optional<ProductVariant> existingVariant = productVariantRepository.findByProductIdAndSku(
                        product.getId(), variantReq.sku()
                    );
                    if (existingVariant.isPresent()) {
                        // Variant with this SKU already exists, skip creating duplicate
                        continue;
                    }

                    variant = ProductVariant.builder()
                        .product(product)
                        .sku(variantReq.sku())
                        .price(variantReq.price())
                        .stockQuantity(variantReq.stockQuantity())
                        .length(variantReq.length())
                        .width(variantReq.width())
                        .height(variantReq.height())
                        .dimensionUnit(variantReq.dimensionUnit())
                        .weight(variantReq.weight())
                        .weightUnit(variantReq.weightUnit())
                        .status(variantReq.status())
                        .optionValues(new ArrayList<>())
                        .images(new ArrayList<>())
                        .build();

                    // Link option values
                    if (variantReq.optionValueIds() != null) {
                        List<ProductOptionValue> optionValues = productOptionValueRepository.findAllById(variantReq.optionValueIds());
                        for (ProductOptionValue pov : optionValues) {
                            ProductVariantOptionValue povLink = ProductVariantOptionValue.builder()
                                .variant(variant)
                                .optionValue(pov)
                                .build();
                            variant.getOptionValues().add(povLink);
                        }
                    }

                    // Create gift box detail if provided
                    if (variantReq.giftBoxDetail() != null) {
                        CreateGiftBoxDetailRequest gbReq = variantReq.giftBoxDetail();
                        GiftBoxVariantDetail giftBoxDetail = GiftBoxVariantDetail.builder()
                            .variant(variant)
                            .innerLength(gbReq.innerLength())
                            .innerWidth(gbReq.innerWidth())
                            .innerHeight(gbReq.innerHeight())
                            .maxWeight(gbReq.maxWeight())
                            .dimensionUnit(gbReq.dimensionUnit())
                            .weightUnit(gbReq.weightUnit())
                            .build();
                        variant.setGiftBoxVariantDetail(giftBoxDetail);
                    }

                    // Create variant policy if provided
                    if (variantReq.variantPolicy() != null) {
                        CreateVariantPolicyRequest policyReq = variantReq.variantPolicy();
                        ProductVariantPolicy policy = ProductVariantPolicy.builder()
                            .variant(variant)
                            .isPerishable(policyReq.isPerishable())
                            .shelfLifeHours(policyReq.shelfLifeHours())
                            .returnPolicy(policyReq.returnPolicy())
                            .advancePaymentPercent(policyReq.advancePaymentPercent())
                            .build();
                        variant.setVariantPolicy(policy);
                    }

                    // Create images if provided
                    if (variantReq.images() != null) {
                        for (VariantImageRequest imgReq : variantReq.images()) {
                            ProductImage image = ProductImage.builder()
                                .productVariant(variant)
                                .imageUrl(imgReq.imageUrl())
                                .secureUrl(imgReq.secureUrl())
                                .publicId(imgReq.publicId())
                                .isPrimary(imgReq.isPrimary())
                                .displayOrder(imgReq.displayOrder())
                                .build();
                            variant.getImages().add(image);
                        }
                    }

                    product.getVariants().add(variant);
                } else {
                    // Update existing variant
                    variant = productVariantRepository.findById(variantReq.id())
                        .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", variantReq.id()));

                    // Ensure variant belongs to this product
                    if (!variant.getProduct().getId().equals(productId)) {
                        throw new ResourceNotFoundException("Product Variant", "product", productId);
                    }

                    if (variantReq.sku() != null) {
                        variant.setSku(variantReq.sku());
                    }
                    if (variantReq.price() != null) {
                        variant.setPrice(variantReq.price());
                    }
                    if (variantReq.stockQuantity() != null) {
                        variant.setStockQuantity(variantReq.stockQuantity());
                    }
                    if (variantReq.length() != null) {
                        variant.setLength(variantReq.length());
                    }
                    if (variantReq.width() != null) {
                        variant.setWidth(variantReq.width());
                    }
                    if (variantReq.height() != null) {
                        variant.setHeight(variantReq.height());
                    }
                    if (variantReq.dimensionUnit() != null) {
                        variant.setDimensionUnit(variantReq.dimensionUnit());
                    }
                    if (variantReq.weight() != null) {
                        variant.setWeight(variantReq.weight());
                    }
                    if (variantReq.weightUnit() != null) {
                        variant.setWeightUnit(variantReq.weightUnit());
                    }
                    if (variantReq.status() != null) {
                        variant.setStatus(variantReq.status());
                    }

                    // Update images if provided
                    if (variantReq.images() != null) {
                        // Delete old variant images that are no longer in the request
                        deleteRemovedImages(
                            variant.getImages(),
                            variantReq.images()
                        );
                        variant.getImages().clear();
                        for (VariantImageRequest imgReq : variantReq.images()) {
                            ProductImage image = ProductImage.builder()
                                .productVariant(variant)
                                .imageUrl(imgReq.imageUrl())
                                .secureUrl(imgReq.secureUrl())
                                .publicId(imgReq.publicId())
                                .isPrimary(imgReq.isPrimary())
                                .displayOrder(imgReq.displayOrder())
                                .build();
                            variant.getImages().add(image);
                        }
                    }

                    // Update gift box detail if provided
                    if (variantReq.giftBoxDetail() != null) {
                        CreateGiftBoxDetailRequest gbReq = variantReq.giftBoxDetail();
                        GiftBoxVariantDetail giftBoxDetail = variant.getGiftBoxVariantDetail();
                        if (giftBoxDetail == null) {
                            giftBoxDetail = GiftBoxVariantDetail.builder().variant(variant).build();
                            variant.setGiftBoxVariantDetail(giftBoxDetail);
                        }
                        if (gbReq.innerLength() != null) giftBoxDetail.setInnerLength(gbReq.innerLength());
                        if (gbReq.innerWidth() != null) giftBoxDetail.setInnerWidth(gbReq.innerWidth());
                        if (gbReq.innerHeight() != null) giftBoxDetail.setInnerHeight(gbReq.innerHeight());
                        if (gbReq.maxWeight() != null) giftBoxDetail.setMaxWeight(gbReq.maxWeight());
                        if (gbReq.dimensionUnit() != null) giftBoxDetail.setDimensionUnit(gbReq.dimensionUnit());
                        if (gbReq.weightUnit() != null) giftBoxDetail.setWeightUnit(gbReq.weightUnit());
                    }

                    // Update variant policy if provided
                    if (variantReq.variantPolicy() != null) {
                        CreateVariantPolicyRequest policyReq = variantReq.variantPolicy();
                        ProductVariantPolicy policy = variant.getVariantPolicy();
                        if (policy == null) {
                            policy = ProductVariantPolicy.builder().variant(variant).build();
                            variant.setVariantPolicy(policy);
                        }
                        if (policyReq.isPerishable() != null) policy.setIsPerishable(policyReq.isPerishable());
                        if (policyReq.shelfLifeHours() != null) policy.setShelfLifeHours(policyReq.shelfLifeHours());
                        if (policyReq.returnPolicy() != null) policy.setReturnPolicy(policyReq.returnPolicy());
                        if (policyReq.advancePaymentPercent() != null) policy.setAdvancePaymentPercent(policyReq.advancePaymentPercent());
                    }
                }
            }
        }

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        // Delete all images from Cloudinary before deleting from DB
        // Common images
        List<ProductImage> commonImages = productImageRepository.findByProductId(productId);
        for (ProductImage img : commonImages) {
            if (img.getPublicId() != null) {
                cloudinaryService.deleteImage(img.getPublicId());
            }
        }

        // Variant images
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        for (ProductVariant variant : variants) {
            List<ProductImage> variantImages = productImageRepository.findByProductVariantId(variant.getId());
            for (ProductImage img : variantImages) {
                if (img.getPublicId() != null) {
                    cloudinaryService.deleteImage(img.getPublicId());
                }
            }
        }

        productRepository.deleteById(productId);
    }

    @Transactional
    public void deleteVariant(UUID productId, UUID variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
            .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", variantId));

        // Ensure variant belongs to this product
        if (!variant.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Product Variant", "product", productId);
        }

        // Delete variant images from Cloudinary
        List<ProductImage> variantImages = productImageRepository.findByProductVariantId(variantId);
        for (ProductImage img : variantImages) {
            if (img.getPublicId() != null) {
                cloudinaryService.deleteImage(img.getPublicId());
            }
        }

        // Variant has orphanRemoval=true on optionValues, so deleting the variant cascades to links
        ProductVariantPolicy policy = productVariantPolicyRepository.findByVariantId(variantId);
        if (policy != null) {
            productVariantPolicyRepository.delete(policy);
        }

        GiftBoxVariantDetail giftBoxDetail = giftBoxVariantDetailRepository.findByVariantId(variantId);
        if (giftBoxDetail != null) {
            giftBoxVariantDetailRepository.delete(giftBoxDetail);
        }

        productImageRepository.deleteByProductVariantId(variantId);

        // Remove variant from product and delete
        variant.getProduct().getVariants().remove(variant);
        productVariantRepository.delete(variant);
    }

    @Transactional
    public ProductVariant updateVariant(UUID variantId, UpdateVariantRequest request) {
        ProductVariant variant = productVariantRepository.findById(variantId)
            .orElseThrow(() -> new ResourceNotFoundException("Product Variant", "id", variantId));

        if (request.sku() != null) variant.setSku(request.sku());
        if (request.price() != null) variant.setPrice(request.price());
        if (request.stockQuantity() != null) variant.setStockQuantity(request.stockQuantity());
        if (request.length() != null) variant.setLength(request.length());
        if (request.width() != null) variant.setWidth(request.width());
        if (request.height() != null) variant.setHeight(request.height());
        if (request.dimensionUnit() != null) variant.setDimensionUnit(request.dimensionUnit());
        if (request.weight() != null) variant.setWeight(request.weight());
        if (request.weightUnit() != null) variant.setWeightUnit(request.weightUnit());
        if (request.status() != null) variant.setStatus(request.status());

        // Update option values if provided
        if (request.optionValueIds() != null) {
            // Clear existing option value links — orphanRemoval will delete them
            variant.getOptionValues().clear();

            // Add new links
            List<ProductOptionValue> optionValues = productOptionValueRepository.findAllById(request.optionValueIds());
            for (ProductOptionValue pov : optionValues) {
                ProductVariantOptionValue povLink = ProductVariantOptionValue.builder()
                    .variant(variant)
                    .optionValue(pov)
                    .build();
                variant.getOptionValues().add(povLink);
            }
        }

        // Update images if provided
        if (request.images() != null) {
            // Delete old variant images that are no longer in the request
            deleteRemovedImages(
                variant.getImages(),
                request.images()
            );
            productImageRepository.deleteByProductVariantId(variantId);
            variant.getImages().clear();
            for (VariantImageRequest imgReq : request.images()) {
                ProductImage image = ProductImage.builder()
                    .productVariant(variant)
                    .imageUrl(imgReq.imageUrl())
                    .secureUrl(imgReq.secureUrl())
                    .publicId(imgReq.publicId())
                    .isPrimary(imgReq.isPrimary())
                    .displayOrder(imgReq.displayOrder())
                    .build();
                variant.getImages().add(image);
            }
        }

        // Update gift box detail if provided
        if (request.giftBoxDetail() != null) {
            CreateGiftBoxDetailRequest gbReq = request.giftBoxDetail();
            GiftBoxVariantDetail giftBoxDetail = variant.getGiftBoxVariantDetail();
            if (giftBoxDetail == null) {
                giftBoxDetail = GiftBoxVariantDetail.builder().variant(variant).build();
                variant.setGiftBoxVariantDetail(giftBoxDetail);
            }
            if (gbReq.innerLength() != null) giftBoxDetail.setInnerLength(gbReq.innerLength());
            if (gbReq.innerWidth() != null) giftBoxDetail.setInnerWidth(gbReq.innerWidth());
            if (gbReq.innerHeight() != null) giftBoxDetail.setInnerHeight(gbReq.innerHeight());
            if (gbReq.maxWeight() != null) giftBoxDetail.setMaxWeight(gbReq.maxWeight());
            if (gbReq.dimensionUnit() != null) giftBoxDetail.setDimensionUnit(gbReq.dimensionUnit());
            if (gbReq.weightUnit() != null) giftBoxDetail.setWeightUnit(gbReq.weightUnit());
        }

        // Update variant policy if provided
        if (request.variantPolicy() != null) {
            CreateVariantPolicyRequest policyReq = request.variantPolicy();
            ProductVariantPolicy policy = variant.getVariantPolicy();
            if (policy == null) {
                policy = ProductVariantPolicy.builder().variant(variant).build();
                variant.setVariantPolicy(policy);
            }
            if (policyReq.isPerishable() != null) policy.setIsPerishable(policyReq.isPerishable());
            if (policyReq.shelfLifeHours() != null) policy.setShelfLifeHours(policyReq.shelfLifeHours());
            if (policyReq.returnPolicy() != null) policy.setReturnPolicy(policyReq.returnPolicy());
            if (policyReq.advancePaymentPercent() != null) policy.setAdvancePaymentPercent(policyReq.advancePaymentPercent());
        }

        return productVariantRepository.save(variant);
    }

    /**
     * Compare old images with new images and delete from Cloudinary any old image
     * whose publicId is no longer present in the new image list.
     * Uses publicId as the unique identifier to avoid accidentally deleting the wrong image.
     */
    private void deleteRemovedImages(List<ProductImage> oldImages, List<VariantImageRequest> newImages) {
        if (oldImages == null || oldImages.isEmpty()) {
            return;
        }
        if (newImages == null || newImages.isEmpty()) {
            // All old images are removed
            for (ProductImage img : oldImages) {
                if (img.getPublicId() != null) {
                    cloudinaryService.deleteImage(img.getPublicId());
                }
            }
            return;
        }
        // Collect publicIds of new images
        java.util.Set<String> newPublicIds = new java.util.HashSet<>();
        for (VariantImageRequest imgReq : newImages) {
            if (imgReq.publicId() != null) {
                newPublicIds.add(imgReq.publicId());
            }
        }
        // Delete old images whose publicId is not in the new list
        for (ProductImage img : oldImages) {
            if (img.getPublicId() != null && !newPublicIds.contains(img.getPublicId())) {
                cloudinaryService.deleteImage(img.getPublicId());
            }
        }
    }

    private CustomComponentType resolveCustomComponentType(
            ProductType productType, CustomComponentType requestedType) {
        CustomComponentType componentType = requestedType != null ? requestedType : switch (productType) {
            case READY_MADE_GIFT_BOX -> CustomComponentType.BOX;
            case PACKAGING_ACCESSORY -> CustomComponentType.ACCESSORY;
            case GREETING_CARD -> CustomComponentType.GREETING_CARD;
            default -> null;
        };
        if (componentType == null) {
            return null;
        }

        boolean validForProductType = switch (componentType) {
            case BOX -> productType == ProductType.READY_MADE_GIFT_BOX;
            case GREETING_CARD -> productType == ProductType.GREETING_CARD;
            case WRAPPING, RIBBON, FILLER, ACCESSORY -> productType == ProductType.PACKAGING_ACCESSORY;
        };
        if (!validForProductType) {
            throw new BadRequestException("Custom component type " + componentType
                    + " is not valid for product type " + productType);
        }
        return componentType;
    }
}
