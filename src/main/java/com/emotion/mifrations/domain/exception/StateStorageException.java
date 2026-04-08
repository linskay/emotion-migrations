package com.emotion.mifrations.domain.exception;

public class StateStorageException extends RuntimeException {
    public StateStorageException(String message) {
        super(message);
    }

    public StateStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
