package elin.le.giftly.product.entity;

import elin.le.giftly.common.enums.ComponentRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "bundle_component")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BundleComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bundle_variant_id", nullable = false)
    private UUID bundleVariantId;

    @Column(name = "component_variant_id", nullable = false)
    private UUID componentVariantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_role", nullable = false)
    private ComponentRole componentRole;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;
}
