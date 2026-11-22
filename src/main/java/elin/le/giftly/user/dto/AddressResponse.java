package elin.le.giftly.user.dto;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String recipientName,
        String phone,
        String addressLine,
        String ward,
        String district,
        String city,
        Boolean isDefault
) {}
