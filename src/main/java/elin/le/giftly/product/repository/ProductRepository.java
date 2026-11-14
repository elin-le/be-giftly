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

    // Cursor-based pagination with optional filters
    @Query(value = """
            SELECT p FROM Product p
            LEFT JOIN p.category c
            WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            AND (:keyword IS NULL OR (p.name ILIKE CONCAT('%', :keyword, '%') OR p.description ILIKE CONCAT('%', :keyword, '%')))
            AND (:cursor IS NULL OR p.id < :cursor)
            ORDER BY p.id DESC
            """)
    List<Product> findProductsCursor(
            @Param("categoryId") UUID categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            @Param("cursor") UUID cursor,
            org.springframework.data.domain.Pageable pageable
    );

    // Native query for fuzzy search with pg_trgm similarity
    @Query(value = """
            SELECT p.* FROM product p
            WHERE (p.name % :keyword OR p.description % :keyword)
            AND (:categoryId IS NULL::uuid OR p.category_id = :categoryId)
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            AND (:cursor IS NULL::uuid OR p.id < :cursor)
            ORDER BY p.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Product> fuzzySearchProductsNative(
            @Param("keyword") String keyword,
            @Param("categoryId") UUID categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("cursor") UUID cursor,
            @Param("limit") int limit
    );
}
