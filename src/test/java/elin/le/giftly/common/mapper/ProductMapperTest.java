package elin.le.giftly.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.common.enums.CustomComponentType;
import elin.le.giftly.common.enums.VariantStatus;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.entity.ProductVariant;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductMapperTest {

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void includesVariantDimensionsAndWeightInProductResponse() {
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Test product")
                .productType(ProductType.REGULAR)
                .customComponentType(CustomComponentType.RIBBON)
                .status(ProductStatus.AVAILABLE)
                .commonImages(new ArrayList<>())
                .build();
        ProductVariant variant = ProductVariant.builder()
                .id(UUID.randomUUID())
                .sku("TEST-SKU")
                .price(new BigDecimal("12.34"))
                .stockQuantity(4)
                .length(25.0)
                .width(15.0)
                .height(8.0)
                .dimensionUnit("cm")
                .weight(2.5)
                .weightUnit("kg")
                .status(VariantStatus.AVAILABLE)
                .optionValues(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        JsonNode productResponse = objectMapper.valueToTree(
                productMapper.toResponseWithDetails(product, List.of(variant))
        );
        JsonNode variantResponse = productResponse.path("variants").get(0);

        assertEquals("RIBBON", productResponse.path("customComponentType").asText());
        assertEquals(25.0, variantResponse.path("length").asDouble());
        assertEquals(15.0, variantResponse.path("width").asDouble());
        assertEquals(8.0, variantResponse.path("height").asDouble());
        assertEquals("cm", variantResponse.path("dimensionUnit").asText());
        assertEquals(2.5, variantResponse.path("weight").asDouble());
        assertEquals("kg", variantResponse.path("weightUnit").asText());
    }
}