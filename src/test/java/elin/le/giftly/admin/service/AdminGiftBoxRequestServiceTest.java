package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.ReviewGiftBoxRequest;
import elin.le.giftly.common.enums.GiftBoxReviewStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.giftbox.dto.GiftBoxResponse;
import elin.le.giftly.giftbox.entity.GiftBoxConfig;
import elin.le.giftly.giftbox.repository.GiftBoxConfigRepository;
import elin.le.giftly.giftbox.service.GiftBoxService;
import elin.le.giftly.order.repository.OrderItemRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AdminGiftBoxRequestServiceTest {

    private final GiftBoxConfigRepository configRepository = mock(GiftBoxConfigRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    private final GiftBoxService giftBoxService = mock(GiftBoxService.class);
    private final AdminGiftBoxRequestService service = new AdminGiftBoxRequestService(
            configRepository, userRepository, orderItemRepository, giftBoxService);

    private final UUID requestId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final GiftBoxConfig config = GiftBoxConfig.builder()
            .id(requestId)
            .userId(customerId)
            .reviewStatus(GiftBoxReviewStatus.PENDING)
            .build();

    @BeforeEach
    void setUp() {
        when(configRepository.findById(requestId)).thenReturn(Optional.of(config));
        when(userRepository.findById(customerId)).thenReturn(Optional.of(
                User.builder().id(customerId).email("customer@example.test").build()));
        when(giftBoxService.getGiftBoxResponseForAdmin(requestId)).thenReturn(
                new GiftBoxResponse(requestId, null, java.util.List.of(), java.util.List.of(), null));
        when(configRepository.save(any(GiftBoxConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void adminCanReviewRequestAndReturnCustomerDetails() {
        var result = service.reviewRequest(requestId,
                new ReviewGiftBoxRequest(GiftBoxReviewStatus.REVISION_REQUIRED, "Please change the ribbon"));

        assertEquals(GiftBoxReviewStatus.REVISION_REQUIRED, result.reviewStatus());
        assertEquals("Please change the ribbon", result.reviewNote());
        assertEquals("customer@example.test", result.customerEmail());
        verify(configRepository).save(config);
    }

    @Test
    void doesNotDeleteGiftBoxRequestReferencedByAnOrder() {
        when(orderItemRepository.existsByGiftBoxConfigId(requestId)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.deleteRequest(requestId));
        verify(configRepository, never()).delete(config);
    }

    @Test
    void deletesUnusedGiftBoxRequest() {
        when(orderItemRepository.existsByGiftBoxConfigId(requestId)).thenReturn(false);

        service.deleteRequest(requestId);

        verify(configRepository).delete(config);
    }
}