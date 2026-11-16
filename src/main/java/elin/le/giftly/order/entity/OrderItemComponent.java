package elin.le.giftly.order.entity;

import elin.le.giftly.common.enums.ComponentRole;
import elin.le.giftly.common.enums.MessageRenderingMethod;
import elin.le.giftly.common.enums.ReturnPolicy;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item_component")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Column(name = "product_variant_id")
    private UUID productVariantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_role")
    private ComponentRole componentRole;

    @Column(name = "product_name_snapshot")
    private String productNameSnapshot;

    @Column(name = "sku_snapshot")
    private String skuSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variant_snapshot")
    private String variantSnapshot;

    private Integer quantity;

    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "is_complimentary")
    private Boolean isComplimentary;

    @Column(name = "message_text")
    private String messageText;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_rendering_method")
    private MessageRenderingMethod messageRenderingMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_policy_snapshot")
    private ReturnPolicy returnPolicySnapshot;

    @Column(name = "advance_payment_percent_snapshot", precision = 5, scale = 2)
    private BigDecimal advancePaymentPercentSnapshot;
}
