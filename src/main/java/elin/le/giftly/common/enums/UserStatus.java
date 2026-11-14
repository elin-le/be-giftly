package elin.le.giftly.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User status", example = "ACTIVE")
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
