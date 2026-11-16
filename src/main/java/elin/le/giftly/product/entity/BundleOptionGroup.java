package elin.le.giftly.product.entity;

import elin.le.giftly.common.enums.GroupType;
import elin.le.giftly.common.enums.SelectionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bundle_option_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BundleOptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bundle_variant_id", nullable = false)
    private UUID bundleVariantId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false)
    private GroupType groupType;

    @Enumerated(EnumType.STRING)
    @Column(name = "selection_type", nullable = false)
    private SelectionType selectionType;

    @Column(name = "min_selections")
    private Integer minSelections;

    @Column(name = "max_selections")
    private Integer maxSelections;

    @Column(name = "is_required")
    private Boolean isRequired;

    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BundleOption> options = new ArrayList<>();
}
