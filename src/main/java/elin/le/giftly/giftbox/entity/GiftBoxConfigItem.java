package elin.le.giftly.giftbox.entity;

import elin.le.giftly.common.enums.MessageRenderingMethod;
import elin.le.giftly.common.enums.CustomComponentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "gift_box_config_item", uniqueConstraints = @UniqueConstraint(
        name = "uk_gbci_config_component_type", columnNames = {"config_id", "component_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftBoxConfigItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id", nullable = false)
    @JsonIgnore
    private GiftBoxConfig config;

    @Column(name = "product_variant_id", nullable = false)
    private UUID productVariantId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", length = 30)
    private CustomComponentType componentType;

    @Column(name = "message_text")
    private String messageText;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_rendering_method")
    private MessageRenderingMethod messageRenderingMethod;
}
