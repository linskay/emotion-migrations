package com.emotion.mifrations.domain.exception;

public class PostUpdateException extends RuntimeException {
    public PostUpdateException(String message) {
        super(message);
    }

    public PostUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
