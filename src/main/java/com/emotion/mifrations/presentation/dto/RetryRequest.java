package com.emotion.mifrations.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Запрос на безопасный retry/перепубликацию поста")
public record RetryRequest(
        @NotNull @Positive
        @Schema(description = "ID Telegram-канала", example = "-1001234567890")
        Long telegramChannelId,
        @NotNull @Positive
        @Schema(description = "ID сообщения Telegram", example = "77")
        Long telegramMessageId) {
}
