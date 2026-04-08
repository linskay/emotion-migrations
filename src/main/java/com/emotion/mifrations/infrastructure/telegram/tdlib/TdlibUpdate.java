package com.emotion.mifrations.infrastructure.telegram.tdlib;

import java.time.Instant;
import java.util.List;

import com.emotion.mifrations.domain.model.MediaType;

public record TdlibUpdate(long channelId,
                          long messageId,
                          String text,
                          String sourceLink,
                          List<TdlibMedia> media,
                          TdlibPoll poll,
                          boolean edited,
                          long editVersion,
                          String mediaGroupId,
                          Instant publishedAt) {
    public record TdlibMedia(String mediaId, MediaType type, String remoteUrl, int order) {}
    public record TdlibPoll(String question, List<String> options, boolean quiz, Integer correctOption) {}
}
