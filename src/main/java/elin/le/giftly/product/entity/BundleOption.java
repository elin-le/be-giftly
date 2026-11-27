package elin.le.giftly.product.entity;

import elin.le.giftly.common.enums.PricingMode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "bundle_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BundleOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_group_id", nullable = false)
    @JsonIgnore
    private BundleOptionGroup optionGroup;

    @Column(name = "product_variant_id", nullable = false)
    private UUID productVariantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_mode", nullable = false)
    private PricingMode pricingMode;

    @Column(name = "fixed_price", precision = 19, scale = 2)
    private BigDecimal fixedPrice;
}
