package elin.le.giftly.cart.entity;

import elin.le.giftly.common.enums.MessageRenderingMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "cart_item_option_selection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemOptionSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id", nullable = false)
    @JsonIgnore
    private CartItem cartItem;

    @Column(name = "bundle_option_id", nullable = false)
    private UUID bundleOptionId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "message_text")
    private String messageText;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_rendering_method")
    private MessageRenderingMethod messageRenderingMethod;
}
