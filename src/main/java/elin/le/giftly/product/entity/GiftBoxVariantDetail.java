package elin.le.giftly.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "gift_box_variant_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftBoxVariantDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false, unique = true)
    @JsonIgnore
    private ProductVariant variant;

    @Column(name = "inner_length")
    private Double innerLength;

    @Column(name = "inner_width")
    private Double innerWidth;

    @Column(name = "inner_height")
    private Double innerHeight;

    @Column(name = "max_weight", precision = 19, scale = 2)
    private BigDecimal maxWeight;

    @Column(name = "dimension_unit")
    private String dimensionUnit;

    @Column(name = "weight_unit")
    private String weightUnit;
}
