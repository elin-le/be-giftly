package elin.le.giftly.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product_option_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    @JsonIgnore
    private ProductOption option;

    @Column(nullable = false)
    private String value;

    @Column(name = "color_hex")
    private String colorHex;

    /**
     * Price adjustment added to the variant's base price when this option value is selected.
     * Default: 0 (no price change).
     * Example: "Size L" might have +5000, "Color Red" might have +0.
     * Final variant price = variant.basePrice + sum of all linked option values' priceAdjustment.
     */
    @Column(name = "price_adjustment", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    /**
     * Links from this option value to product variants.
     * When this option value is deleted, all variant links are cascade-deleted.
     */
    @OneToMany(mappedBy = "optionValue", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<ProductVariantOptionValue> variantLinks = new ArrayList<>();
}
