package elin.le.giftly.review.controller;

import elin.le.giftly.common.dto.ApiResponse;
import elin.le.giftly.common.dto.PageResponse;
import elin.le.giftly.review.dto.ReviewResponse;
import elin.le.giftly.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @Test
    void returnsPaginatedReviewsFromTheExistingProductEndpoint() {
        UUID productId = UUID.randomUUID();
        Short rating = 5;
        Pageable pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        ReviewResponse review = new ReviewResponse(
                UUID.randomUUID(), UUID.randomUUID(), "customer@example.test", null, UUID.randomUUID(),
                5, "Great", "Lovely gift", List.of(), null, null);
        Page<ReviewResponse> page = new PageImpl<>(List.of(review), pageable, 11);
        when(reviewService.getReviewsByProductAndRating(productId, rating, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> response =
                reviewController.getReviewsByProduct(productId, rating, pageable);

        PageResponse<ReviewResponse> data = response.getBody().getData();
        assertEquals(1, data.getPage());
        assertEquals(10, data.getSize());
        assertEquals(11, data.getTotalElements());
        assertEquals(2, data.getTotalPages());
        assertEquals(List.of(review), data.getContent());
        verify(reviewService).getReviewsByProductAndRating(productId, rating, pageable);
    }
}