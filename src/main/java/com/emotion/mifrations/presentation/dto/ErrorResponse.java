package com.emotion.mifrations.presentation.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Единый формат ошибки")
public record ErrorResponse(
        @Schema(description = "Момент ошибки", example = "2026-04-07T12:00:00Z")
        Instant timestamp,
        @Schema(description = "Код HTTP статуса", example = "400")
        int status,
        @Schema(description = "Тип ошибки", example = "ConfigurationException")
        String error,
        @Schema(description = "Сообщение ошибки на русском", example = "Некорректная конфигурация")
        String message,
        @Schema(description = "Путь запроса", example = "/api/v1/system/health")
        String path) {
}
