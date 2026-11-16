package elin.le.giftly.giftbox.entity;

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

    @Column(name = "box_variant_id", nullable = false)
    private UUID boxVariantId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "config", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GiftBoxConfigItem> items = new ArrayList<>();
}
