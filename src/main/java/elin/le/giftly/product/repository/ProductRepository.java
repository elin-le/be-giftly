package elin.le.giftly.product.repository;

import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByProductType(ProductType productType, Pageable pageable);
    List<Product> findByCategoryIdAndStatus(UUID categoryId, ProductStatus status);

    // Product prices are stored on variants; all supplied filters are combined in this cursor query.
    @Query(value = """
            SELECT p.* FROM product p
            WHERE (CAST(:categoryId AS uuid) IS NULL OR p.category_id = CAST(:categoryId AS uuid))
            AND (CAST(:keyword AS varchar) IS NULL OR (
                unaccent(lower(p.name)) LIKE '%' || unaccent(lower(CAST(:keyword AS varchar))) || '%'
                OR unaccent(lower(COALESCE(p.description, ''))) LIKE '%' || unaccent(lower(CAST(:keyword AS varchar))) || '%'
                OR regexp_replace(unaccent(lower(concat_ws(' ', p.name, p.description))), '[[:space:]]+', '', 'g')
                   LIKE '%' || regexp_replace(unaccent(lower(CAST(:keyword AS varchar))), '[[:space:]]+', '', 'g') || '%'
                OR unaccent(lower(concat_ws(' ', p.name, p.description))) % unaccent(lower(CAST(:keyword AS varchar)))
                OR (
                    SELECT bool_and(
                        unaccent(lower(concat_ws(' ', p.name, p.description))) LIKE '%' || word || '%'
                        OR unaccent(lower(concat_ws(' ', p.name, p.description))) % word
                    )
                    FROM unnest(regexp_split_to_array(unaccent(lower(CAST(:keyword AS varchar))), '[[:space:]]+')) AS words(word)
                    WHERE word <> ''
                )
            ))
            AND (CAST(:productType AS varchar) IS NULL OR p.product_type = CAST(:productType AS varchar))
            AND (CAST(:customComponentType AS varchar) IS NULL
                 OR p.custom_component_type = CAST(:customComponentType AS varchar))
            AND (CAST(:customBoxEligible AS boolean) IS NULL
                 OR p.is_custom_box_eligible = CAST(:customBoxEligible AS boolean))
            AND (
                (CAST(:minPrice AS numeric) IS NULL AND CAST(:maxPrice AS numeric) IS NULL)
                OR EXISTS (
                    SELECT 1 FROM product_variant v
                    WHERE v.product_id = p.id
                    AND (CAST(:minPrice AS numeric) IS NULL OR v.price >= CAST(:minPrice AS numeric))
                    AND (CAST(:maxPrice AS numeric) IS NULL OR v.price <= CAST(:maxPrice AS numeric))
                )
            )
            AND (CAST(:cursor AS uuid) IS NULL OR (p.created_at, p.id) < (
                SELECT cursor_product.created_at, cursor_product.id
                FROM product cursor_product
                WHERE cursor_product.id = CAST(:cursor AS uuid)
            ))
            ORDER BY p.created_at DESC, p.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Product> searchProductsCursor(
            @Param("categoryId") UUID categoryId,
            @Param("keyword") String keyword,
            @Param("productType") String productType,
            @Param("customComponentType") String customComponentType,
            @Param("customBoxEligible") Boolean customBoxEligible,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("cursor") UUID cursor,
            @Param("limit") int limit
    );
}
