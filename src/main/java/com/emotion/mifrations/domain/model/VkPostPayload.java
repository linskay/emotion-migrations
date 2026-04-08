package com.emotion.mifrations.domain.model;

import java.util.List;

import lombok.Builder;

@Builder
public record VkPostPayload(String text, List<String> attachments, String pollAttachment) {
}
