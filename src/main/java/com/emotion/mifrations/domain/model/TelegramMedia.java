package com.emotion.mifrations.domain.model;

import lombok.Builder;

@Builder
public record TelegramMedia(String mediaId, MediaType type, String fileName, String url, int order) {
}
