package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.*;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.*;
import elin.le.giftly.product.repository.CategoryRepository;
import elin.le.giftly.product.repository.ProductOptionValueRepository;
import elin.le.giftly.product.repository.ProductRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private static final Logger log = LoggerFactory.getLogger(AdminProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final ProductVariantRepository productVariantRepository;

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
            .status(request.status())
            .isCustomBoxEligible(request.isCustomBoxEligible())
            .variants(new ArrayList<>())
            .build();

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
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        if (request.isCustomBoxEligible() != null) {
            product.setIsCustomBoxEligible(request.isCustomBoxEligible());
        }

        // Update variants if provided
        if (request.variants() != null && !request.variants().isEmpty()) {
            for (UpdateVariantRequest variantReq : request.variants()) {
                ProductVariant variant = productVariantRepository.findById(variantReq.id())
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

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        productRepository.deleteById(productId);
    }
}
