package elin.le.giftly.product.entity;

import elin.le.giftly.common.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product_variant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    // Physical dimensions of the variant
    private Double length;
    private Double width;
    private Double height;

    @Enumerated(EnumType.STRING)
    @Column(name = "dimension_unit")
    private String dimensionUnit;

    private Double weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_unit")
    private String weightUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariantOptionValue> optionValues = new ArrayList<>();

    // One-to-one relationships for variant-specific details
    @OneToOne(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private GiftBoxVariantDetail giftBoxVariantDetail;

    @OneToOne(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductVariantPolicy variantPolicy;
}
