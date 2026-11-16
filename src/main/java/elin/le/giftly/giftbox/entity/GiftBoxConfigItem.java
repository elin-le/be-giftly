package elin.le.giftly.giftbox.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "gift_box_config_item")
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
    private GiftBoxConfig config;

    @Column(name = "product_variant_id", nullable = false)
    private UUID productVariantId;

    @Column(nullable = false)
    private Integer quantity;
}
