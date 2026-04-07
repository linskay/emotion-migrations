package com.emotion.mifrations.domain.exception;

public class PollCreationException extends RuntimeException {
    public PollCreationException(String message) {
        super(message);
    }

    public PollCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
