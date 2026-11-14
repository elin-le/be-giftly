package elin.le.giftly.admin.dto;

import elin.le.giftly.common.enums.UserStatus;

public record UpdateUserStatusRequest(
    UserStatus status
) {}
