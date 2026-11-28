package elin.le.giftly.admin.dto;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class UpdateCategoryRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

    @Test
    void acceptsNullRemoveImage() throws Exception {
        UpdateCategoryRequest request = objectMapper.readValue(
                """
                {"name":"Gấu bông","removeImage":null}
                """,
                UpdateCategoryRequest.class);

        assertNull(request.removeImage());
    }
}