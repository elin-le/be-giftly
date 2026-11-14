package elin.le.giftly.giftbox.entity;

import elin.le.giftly.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "gift_box_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftBoxConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_product_id", nullable = false)
    private Product boxProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wrapping_product_id")
    private Product wrappingProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ribbon_product_id")
    private Product ribbonProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_product_id")
    private Product cardProduct;

    @Column(name = "greeting_message")
    private String greetingMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "config", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GiftBoxItem> items = new ArrayList<>();
}
