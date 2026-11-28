package elin.le.giftly.admin.service;

import elin.le.giftly.admin.dto.AdminGiftBoxRequestResponse;
import elin.le.giftly.admin.dto.CreateAdminGiftBoxRequest;
import elin.le.giftly.admin.dto.ReviewGiftBoxRequest;
import elin.le.giftly.common.enums.GiftBoxReviewStatus;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.giftbox.dto.GiftBoxResponse;
import elin.le.giftly.giftbox.dto.UpdateGiftBoxRequest;
import elin.le.giftly.giftbox.entity.GiftBoxConfig;
import elin.le.giftly.giftbox.repository.GiftBoxConfigRepository;
import elin.le.giftly.giftbox.service.GiftBoxService;
import elin.le.giftly.order.repository.OrderItemRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminGiftBoxRequestService {

    private final GiftBoxConfigRepository giftBoxConfigRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final GiftBoxService giftBoxService;

    @Transactional
    public AdminGiftBoxRequestResponse createRequest(CreateAdminGiftBoxRequest request) {
        if (!userRepository.existsById(request.customerId())) {
            throw new ResourceNotFoundException("User", "id", request.customerId());
        }
        GiftBoxResponse configuration = giftBoxService.createGiftBoxForUser(
                request.customerId(), request.toGiftBoxRequest());
        return getRequestById(configuration.id());
    }

    @Transactional(readOnly = true)
    public Page<AdminGiftBoxRequestResponse> getRequests(GiftBoxReviewStatus status, Pageable pageable) {
        Page<GiftBoxConfig> configs = status == null
                ? giftBoxConfigRepository.findAll(pageable)
                : giftBoxConfigRepository.findByReviewStatus(status, pageable);
        return configs.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AdminGiftBoxRequestResponse getRequestById(UUID requestId) {
        GiftBoxConfig config = giftBoxConfigRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("GiftBoxRequest", "id", requestId));
        return toResponse(config);
    }

    @Transactional
    public AdminGiftBoxRequestResponse updateRequest(UUID requestId, UpdateGiftBoxRequest request) {
        giftBoxService.updateGiftBoxForAdmin(requestId, request);
        return getRequestById(requestId);
    }

    @Transactional
    public AdminGiftBoxRequestResponse reviewRequest(UUID requestId, ReviewGiftBoxRequest request) {
        GiftBoxConfig config = giftBoxConfigRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("GiftBoxRequest", "id", requestId));
        config.setReviewStatus(request.status());
        config.setReviewNote(request.note());
        return toResponse(giftBoxConfigRepository.save(config));
    }

    @Transactional
    public void deleteRequest(UUID requestId) {
        GiftBoxConfig config = giftBoxConfigRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("GiftBoxRequest", "id", requestId));
        if (orderItemRepository.existsByGiftBoxConfigId(requestId)) {
            throw new BadRequestException("A gift box request used in an order cannot be deleted");
        }
        giftBoxConfigRepository.delete(config);
    }

    private AdminGiftBoxRequestResponse toResponse(GiftBoxConfig config) {
        User customer = userRepository.findById(config.getUserId()).orElse(null);
        GiftBoxResponse configuration = giftBoxService.getGiftBoxResponseForAdmin(config.getId());
        return new AdminGiftBoxRequestResponse(
                config.getId(),
                config.getUserId(),
                customer == null ? null : customer.getEmail(),
                config.getReviewStatus(),
                config.getReviewNote(),
                config.getCreatedAt(),
                config.getUpdatedAt(),
                configuration
        );
    }
}