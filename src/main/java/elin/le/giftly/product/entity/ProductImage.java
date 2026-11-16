package elin.le.giftly.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "product_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "secure_url", nullable = false)
    private String secureUrl;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "is_thumbnail")
    private Boolean isThumbnail;
}
