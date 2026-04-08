package com.emotion.mifrations.infrastructure.telegram.tdlib;

import java.util.List;

import org.springframework.stereotype.Component;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.model.PostStatus;
import com.emotion.mifrations.domain.model.TelegramMedia;
import com.emotion.mifrations.domain.model.TelegramPoll;
import com.emotion.mifrations.domain.model.TelegramPost;
import com.emotion.mifrations.domain.port.TelegramPostSourcePort;

@Component
public class TdlibTelegramSourceAdapter implements TelegramPostSourcePort {
    private final TdlibGateway tdlibGateway;
    private final AppProperties.Telegram.Tdlib config;

    public TdlibTelegramSourceAdapter(TdlibGateway tdlibGateway, AppProperties properties) {
        this.tdlibGateway = tdlibGateway;
        this.config = properties.telegram().tdlib();
    }

    @Override
    public List<TelegramPost> fetchNewPosts() {
        return tdlibGateway.fetchUpdates(config.fetchBatchSize()).stream()
                .map(this::map)
                .toList();
    }

    private TelegramPost map(TdlibUpdate update) {
        TelegramPoll poll = update.poll() == null ? null : TelegramPoll.builder()
                .question(update.poll().question())
                .options(update.poll().options())
                .quiz(update.poll().quiz())
                .correctOption(update.poll().correctOption())
                .build();

        return TelegramPost.builder()
                .channelId(update.channelId())
                .messageId(update.messageId())
                .text(update.text())
                .sourceLink(update.sourceLink())
                .poll(poll)
                .status(update.edited() ? PostStatus.EDITED : PostStatus.NEW)
                .editVersion(update.editVersion())
                .publishedAt(update.publishedAt())
                .mediaGroupId(update.mediaGroupId())
                .media(update.media().stream()
                        .map(m -> TelegramMedia.builder()
                                .mediaId(m.mediaId())
                                .type(m.type())
                                .url(m.remoteUrl())
                                .order(m.order())
                                .build())
                        .toList())
                .build();
    }
}
