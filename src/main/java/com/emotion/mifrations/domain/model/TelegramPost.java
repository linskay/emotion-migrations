package com.emotion.mifrations.domain.model;

import java.time.Instant;
import java.util.List;

import lombok.Builder;

@Builder
public record TelegramPost(
        long channelId,
        long messageId,
        String sourceLink,
        String text,
        List<TelegramMedia> media,
        TelegramPoll poll,
        PostStatus status,
        long editVersion,
        String mediaGroupId,
        Instant publishedAt
) {

    public boolean hasUnsupportedMedia() {
        return media != null && media.stream().anyMatch(m -> m.type() == MediaType.AUDIO || m.type() == MediaType.DOCUMENT || m.type() == MediaType.VIDEO_NOTE);
    }
}
