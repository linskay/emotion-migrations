package com.emotion.mifrations.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Состояние сервиса")
public record SystemHealthResponse(
        @Schema(description = "Сервис активен", example = "true")
        boolean up,
        @Schema(description = "Описание", example = "Сервис работает штатно")
        String message) {
}
