package com.emotion.mifrations.presentation.api;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emotion.mifrations.application.dto.PublishResult;
import com.emotion.mifrations.application.usecase.ProcessTelegramPostUseCase;
import com.emotion.mifrations.domain.exception.PostUpdateException;
import com.emotion.mifrations.domain.model.PostStatus;
import com.emotion.mifrations.domain.model.TelegramPost;
import com.emotion.mifrations.presentation.dto.RetryRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/operations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Операции", description = "Ручные безопасные операции обслуживания")
public class OperationsController {
    private final ProcessTelegramPostUseCase processTelegramPostUseCase;

    @PostMapping("/retry")
    @Operation(summary = "Ручной retry/перепубликация", description = "Безопасно инициирует переобработку опубликованного сообщения в режиме EDITED")
    public ResponseEntity<PublishResult> retry(@Valid @RequestBody RetryRequest request) {
        TelegramPost post = TelegramPost.builder()
                .channelId(request.telegramChannelId())
                .messageId(request.telegramMessageId())
                .sourceLink("https://t.me/c/" + request.telegramChannelId() + "/" + request.telegramMessageId())
                .status(PostStatus.EDITED)
                .editVersion(System.currentTimeMillis())
                .build();
        PublishResult result = processTelegramPostUseCase.process(post);
        if (!result.published()) {
            throw new PostUpdateException("Retry не выполнил публикацию: " + result.reason());
        }
        return ResponseEntity.ok(result);
    }
}
