package com.emotion.mifrations.infrastructure.telegram.tdlib;

import java.util.List;

import org.springframework.stereotype.Component;

import com.emotion.mifrations.config.AppProperties;

@Component
public class TdlightTdlibGateway implements TdlibGateway {
    private final AppProperties.Telegram.Tdlib config;

    public TdlightTdlibGateway(AppProperties properties) {
        this.config = properties.telegram().tdlib();
    }

    @Override
    public List<TdlibUpdate> fetchUpdates(int maxItems) {
        return List.of();
    }

    public AppProperties.Telegram.Tdlib config() {
        return config;
    }
}
