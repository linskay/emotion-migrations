package com.emotion.mifrations.domain.exception;

public class VkIntegrationException extends RuntimeException {
    public VkIntegrationException(String message) {
        super(message);
    }

    public VkIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
