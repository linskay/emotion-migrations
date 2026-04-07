package com.emotion.mifrations.domain.model;

import java.time.Instant;

import lombok.Builder;

@Builder
public record PostMapping(
        long telegramChannelId,
        long telegramMessageId,
        long vkCommunityId,
        long vkPostId,
        long lastEditVersion,
        Instant updatedAt,
        Instant createdAt
) {
}
