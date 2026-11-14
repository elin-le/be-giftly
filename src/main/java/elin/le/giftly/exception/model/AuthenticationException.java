package elin.le.giftly.exception.model;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {
    private final String message;

    public AuthenticationException(String message) {
        super(message);
        this.message = message;
    }
}
