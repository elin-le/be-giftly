package elin.le.giftly.giftbox.entity;

import elin.le.giftly.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "gift_box_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftBoxItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id", nullable = false)
    private GiftBoxConfig config;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;
}
