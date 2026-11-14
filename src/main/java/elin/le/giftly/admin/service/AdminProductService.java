package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.CreateProductRequest;
import elin.le.giftly.admin.dto.ImageInfo;
import elin.le.giftly.admin.dto.UpdateProductRequest;
import elin.le.giftly.cloudinary.service.CloudinaryService;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.product.entity.Category;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductImage;
import elin.le.giftly.product.repository.CategoryRepository;
import elin.le.giftly.product.repository.ProductImageRepository;
import elin.le.giftly.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
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
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));

        Product product = Product.builder()
            .category(category)
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .stockQuantity(request.stockQuantity())
            .productType(request.productType())
            .status(request.status())
            .build();

        product = productRepository.save(product);

        if (request.images() != null && !request.images().isEmpty()) {
            Product savedProduct = product;
            List<ProductImage> images = request.images().stream()
                .map(info -> ProductImage.builder()
                    .product(savedProduct)
                    .imageUrl(info.secureUrl())
                    .secureUrl(info.secureUrl())
                    .publicId(info.publicId())
                    .isThumbnail(false)
                    .build())
                .toList();
            productImageRepository.saveAll(images);
            product.setImages(images);
        }

        return product;
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
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.stockQuantity() != null) {
            product.setStockQuantity(request.stockQuantity());
        }
        if (request.productType() != null) {
            product.setProductType(request.productType());
        }
        if (request.status() != null) {
            product.setStatus(request.status());
        }

        if (request.images() != null) {
            // Get current images
            List<ProductImage> oldImages = productImageRepository.findByProductId(productId);
            Set<String> oldPublicIds = new HashSet<>();
            for (ProductImage img : oldImages) {
                oldPublicIds.add(img.getPublicId());
            }

            // Build set of new public IDs
            Set<String> newPublicIds = new HashSet<>();
            if (request.images() != null) {
                for (ImageInfo info : request.images()) {
                    newPublicIds.add(info.publicId());
                }
            }

            // Find images that were removed (in old but not in new) and delete from Cloudinary
            Set<String> removedPublicIds = new HashSet<>(oldPublicIds);
            removedPublicIds.removeAll(newPublicIds);
            if (!removedPublicIds.isEmpty()) {
                cloudinaryService.deleteImages(removedPublicIds.stream().toList());
            }

            // Find images that are new (in new but not in old)
            Set<String> addedPublicIds = new HashSet<>(newPublicIds);
            addedPublicIds.removeAll(oldPublicIds);

            // Delete all old DB images and recreate from the new list
            productImageRepository.deleteByProductId(productId);

            Product savedProduct = product;
            List<ProductImage> newImages = request.images().stream()
                .map(info -> ProductImage.builder()
                    .product(savedProduct)
                    .imageUrl(info.secureUrl())
                    .secureUrl(info.secureUrl())
                    .publicId(info.publicId())
                    .isThumbnail(false)
                    .build())
                .toList();
            productImageRepository.saveAll(newImages);
            product.setImages(newImages);
        }

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        // Delete all product images from Cloudinary before removing from DB
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        if (!images.isEmpty()) {
            List<String> publicIds = images.stream()
                .map(ProductImage::getPublicId)
                .toList();
            cloudinaryService.deleteImages(publicIds);
        }
        productRepository.deleteById(productId);
    }
}
