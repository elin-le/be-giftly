package elin.le.giftly.product.service;

import elin.le.giftly.common.mapper.ProductMapper;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.product.repository.ProductVariantRepository;
import elin.le.giftly.review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductServiceFilterTest {

    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final ProductService productService = new ProductService(
            productRepository,
            mock(ProductVariantRepository.class),
            mock(ProductMapper.class),
            mock(ReviewRepository.class)
    );

    @Test
    void combinesKeywordTypeCategoryAndPriceFiltersInRepositoryQuery() {
        UUID categoryId = UUID.randomUUID();
        UUID cursor = UUID.randomUUID();
        BigDecimal minPrice = new BigDecimal("50000");
        BigDecimal maxPrice = new BigDecimal("200000");
        when(productRepository.searchProductsCursor(
                categoryId, "hoa hong", "REGULAR", "RIBBON", null, minPrice, maxPrice, cursor, 21
        )).thenReturn(List.of());

        productService.getProducts(categoryId, " hoa hong ", "mua lẻ", "ribbon", minPrice, maxPrice, cursor.toString(), 20);

        verify(productRepository).searchProductsCursor(
                categoryId, "hoa hong", "REGULAR", "RIBBON", null, minPrice, maxPrice, cursor, 21
        );
    }

    @Test
    void rejectsNegativeOrInvertedPriceRangesBeforeQuerying() {
        assertThrows(BadRequestException.class, () -> productService.getProducts(
                null, null, null, null, new BigDecimal("200"), new BigDecimal("100"), null, 20));
        assertThrows(BadRequestException.class, () -> productService.getProducts(
                null, null, null, null, new BigDecimal("-1"), null, null, 20));

        verifyNoInteractions(productRepository);
    }

    @Test
    void rejectsUnknownCustomComponentTypeBeforeQuerying() {
        assertThrows(BadRequestException.class, () -> productService.getProducts(
                null, null, null, "BOW", null, null, null, 20));

        verifyNoInteractions(productRepository);
    }
}