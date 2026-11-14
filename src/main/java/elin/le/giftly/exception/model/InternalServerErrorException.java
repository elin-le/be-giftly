package elin.le.giftly.exception.model;

import lombok.Getter;

@Getter
public class InternalServerErrorException extends RuntimeException {
    private final String message;

    public InternalServerErrorException(String message) {
        super(message);
        this.message = message;
    }
}
