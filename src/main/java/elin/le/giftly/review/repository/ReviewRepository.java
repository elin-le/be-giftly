package elin.le.giftly.review.repository;

import elin.le.giftly.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByProductId(UUID productId, Pageable pageable);
    Page<Review> findByUserId(UUID userId, Pageable pageable);
    
    Optional<Review> findByOrderItemId(UUID orderItemId);
    boolean existsByOrderItemId(UUID orderItemId);
    
    List<Review> findByProductIdOrderByCreatedAtDesc(UUID productId);
    
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.product.id = :productId")
    double findAverageRatingByProductId(@Param("productId") UUID productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    long countByProductId(@Param("productId") UUID productId);
}
