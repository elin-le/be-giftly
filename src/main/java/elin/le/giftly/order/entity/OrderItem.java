package elin.le.giftly.order.entity;

import elin.le.giftly.common.enums.ReturnPolicy;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_variant_id")
    private UUID productVariantId;

    @Column(name = "gift_box_config_id")
    private UUID giftBoxConfigId;

    @Column(name = "item_name_snapshot", nullable = false)
    private String itemNameSnapshot;

    @Column(name = "sku_snapshot")
    private String skuSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variant_snapshot")
    private String variantSnapshot;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_policy_snapshot")
    private ReturnPolicy returnPolicySnapshot;

    @Column(name = "advance_payment_percent_snapshot", precision = 5, scale = 2)
    private BigDecimal advancePaymentPercentSnapshot;

    @Column(name = "is_perishable_snapshot")
    private Boolean isPerishableSnapshot;

    @Column(name = "shelf_life_hours_snapshot")
    private Integer shelfLifeHoursSnapshot;

    @Column(name = "required_advance_amount", precision = 19, scale = 2)
    private BigDecimal requiredAdvanceAmount;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemComponent> components = new ArrayList<>();
}
