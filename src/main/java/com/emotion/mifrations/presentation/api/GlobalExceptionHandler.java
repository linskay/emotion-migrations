package com.emotion.mifrations.presentation.api;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.emotion.mifrations.domain.exception.ConfigurationException;
import com.emotion.mifrations.domain.exception.DuplicatePostException;
import com.emotion.mifrations.domain.exception.MediaDownloadException;
import com.emotion.mifrations.domain.exception.NotificationDeliveryException;
import com.emotion.mifrations.domain.exception.PollCreationException;
import com.emotion.mifrations.domain.exception.PostAssemblyException;
import com.emotion.mifrations.domain.exception.PostUpdateException;
import com.emotion.mifrations.domain.exception.StateStorageException;
import com.emotion.mifrations.domain.exception.TelegramIntegrationException;
import com.emotion.mifrations.domain.exception.VkIntegrationException;
import com.emotion.mifrations.presentation.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            TelegramIntegrationException.class,
            VkIntegrationException.class,
            MediaDownloadException.class,
            PostAssemblyException.class,
            PollCreationException.class,
            NotificationDeliveryException.class,
            StateStorageException.class
    })
    public ResponseEntity<ErrorResponse> handleIntegration(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex, request.getRequestURI());
    }

    @ExceptionHandler({ConfigurationException.class, PostUpdateException.class})
    public ResponseEntity<ErrorResponse> handleConfig(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex, request.getRequestURI());
    }

    @ExceptionHandler(DuplicatePostException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicatePostException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField)
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("неизвестные поля");
        return build(HttpStatus.BAD_REQUEST,
                new ConfigurationException("Ошибка валидации запроса: " + details),
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                new ConfigurationException("Внутренняя ошибка сервиса"),
                request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, RuntimeException ex, String path) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(ex.getClass().getSimpleName())
                .message(ex.getMessage())
                .path(path)
                .build());
    }
}
