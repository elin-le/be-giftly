package elin.le.giftly.product.service;

import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.exception.model.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTypeFilterTest {

    @Test
    void mapsCustomerFacingProductTypeAliases() {
        assertEquals(new ProductTypeFilter(null, true), ProductTypeFilter.from("custom"));
        assertEquals(new ProductTypeFilter(ProductType.REGULAR, null), ProductTypeFilter.from("mua lẻ"));
        assertEquals(new ProductTypeFilter(ProductType.READY_MADE_GIFT_SET, null),
                ProductTypeFilter.from("set"));
        assertEquals(new ProductTypeFilter(ProductType.READY_MADE_GIFT_BOX, null),
                ProductTypeFilter.from("box"));
    }

    @Test
    void acceptsProductTypeEnumNamesAndIgnoresBlankValues() {
        assertEquals(new ProductTypeFilter(ProductType.PACKAGING_ACCESSORY, null),
                ProductTypeFilter.from("PACKAGING_ACCESSORY"));
        assertEquals(new ProductTypeFilter(null, null), ProductTypeFilter.from("  "));
    }

    @Test
    void rejectsUnknownProductType() {
        assertThrows(BadRequestException.class, () -> ProductTypeFilter.from("unknown"));
    }
}