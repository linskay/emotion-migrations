package com.emotion.mifrations.application.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.exception.ConfigurationException;
import com.emotion.mifrations.domain.model.ChannelBinding;

@Component
public class ChannelResolver {
    private final List<ChannelBinding> bindings;

    public ChannelResolver(AppProperties properties) {
        this.bindings = properties.telegram().channels().stream()
                .map(cfg -> ChannelBinding.builder()
                        .telegramChannelId(cfg.sourceId())
                        .telegramName(cfg.sourceName())
                        .vkCommunityId(cfg.vkCommunityId())
                        .vkName(cfg.vkCommunityName())
                        .build())
                .toList();
    }

    public ChannelBinding resolve(long telegramChannelId) {
        return bindings.stream()
                .filter(b -> b.telegramChannelId() == telegramChannelId)
                .findFirst()
                .orElseThrow(() -> new ConfigurationException("Для Telegram-канала не найдено соответствие VK: " + telegramChannelId));
    }

    public List<ChannelBinding> getBindings() {
        return bindings;
    }
}
