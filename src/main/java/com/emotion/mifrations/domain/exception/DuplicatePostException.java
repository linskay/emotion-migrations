package com.emotion.mifrations.domain.exception;

public class DuplicatePostException extends RuntimeException {
    public DuplicatePostException(String message) {
        super(message);
    }

    public DuplicatePostException(String message, Throwable cause) {
        super(message, cause);
    }
}
