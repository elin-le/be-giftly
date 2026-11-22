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

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByProductType(ProductType productType, Pageable pageable);
    List<Product> findByCategoryIdAndStatus(UUID categoryId, ProductStatus status);

    // Cursor-based pagination with optional filters (price filter removed - price is on Variant)
    // Using native query with explicit type hints to avoid Hibernate/PostgreSQL type mismatch
    @Query(value = """
            SELECT p.* FROM product p
            LEFT JOIN category c ON c.id = p.category_id
            WHERE (CAST(:categoryId AS uuid) IS NULL OR c.id = CAST(:categoryId AS uuid))
            AND (:keyword IS NULL OR (LOWER(p.name) LIKE LOWER('%' || CAST(:keyword AS varchar) || '%') OR LOWER(p.description) LIKE LOWER('%' || CAST(:keyword AS varchar) || '%')))
            AND (CAST(:cursor AS uuid) IS NULL OR p.id < CAST(:cursor AS uuid))
            ORDER BY p.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Product> findProductsCursor(
            @Param("categoryId") UUID categoryId,
            @Param("keyword") String keyword,
            @Param("cursor") UUID cursor,
            @Param("limit") int limit
    );

    // Native query for fuzzy search with pg_trgm similarity
    @Query(value = """
            SELECT p.* FROM product p
            WHERE (p.name % :keyword OR p.description % :keyword)
            AND (CAST(:categoryId AS uuid) IS NULL OR p.category_id = CAST(:categoryId AS uuid))
            AND (CAST(:cursor AS uuid) IS NULL OR p.id < CAST(:cursor AS uuid))
            ORDER BY p.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Product> fuzzySearchProductsNative(
            @Param("keyword") String keyword,
            @Param("categoryId") UUID categoryId,
            @Param("cursor") UUID cursor,
            @Param("limit") int limit
    );
}
