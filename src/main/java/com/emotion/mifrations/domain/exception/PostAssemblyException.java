package com.emotion.mifrations.domain.exception;

public class PostAssemblyException extends RuntimeException {
    public PostAssemblyException(String message) {
        super(message);
    }

    public PostAssemblyException(String message, Throwable cause) {
        super(message, cause);
    }
}
