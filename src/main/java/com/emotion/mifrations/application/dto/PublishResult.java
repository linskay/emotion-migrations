package com.emotion.mifrations.application.dto;

import lombok.Builder;

@Builder
public record PublishResult(boolean published, String reason, Long vkPostId) {
}
