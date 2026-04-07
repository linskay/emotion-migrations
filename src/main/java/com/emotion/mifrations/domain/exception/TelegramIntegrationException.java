package com.emotion.mifrations.domain.exception;

public class TelegramIntegrationException extends RuntimeException {
    public TelegramIntegrationException(String message) {
        super(message);
    }

    public TelegramIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
