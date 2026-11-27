package elin.le.giftly.product.entity;

import elin.le.giftly.common.enums.ReturnPolicy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_variant_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false, unique = true)
    @JsonIgnore
    private ProductVariant variant;

    @Column(name = "is_perishable")
    private Boolean isPerishable;

    @Column(name = "shelf_life_hours")
    private Integer shelfLifeHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_policy")
    private ReturnPolicy returnPolicy;

    @Column(name = "advance_payment_percent", precision = 5, scale = 2)
    private BigDecimal advancePaymentPercent;
}
