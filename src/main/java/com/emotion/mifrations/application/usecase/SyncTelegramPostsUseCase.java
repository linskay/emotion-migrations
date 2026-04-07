package com.emotion.mifrations.application.usecase;

import org.springframework.stereotype.Service;

import com.emotion.mifrations.domain.port.TelegramPostSourcePort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncTelegramPostsUseCase {
    private final TelegramPostSourcePort sourcePort;
    private final ProcessTelegramPostUseCase processTelegramPostUseCase;

    public void sync() {
        sourcePort.fetchNewPosts().forEach(processTelegramPostUseCase::process);
    }
}
