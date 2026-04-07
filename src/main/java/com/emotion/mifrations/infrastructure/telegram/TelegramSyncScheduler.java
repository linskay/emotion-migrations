package com.emotion.mifrations.infrastructure.telegram;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.emotion.mifrations.application.usecase.MaintenanceUseCase;
import com.emotion.mifrations.application.usecase.SyncTelegramPostsUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TelegramSyncScheduler {
    private final SyncTelegramPostsUseCase syncUseCase;
    private final MaintenanceUseCase maintenanceUseCase;

    @Scheduled(fixedDelayString = "${app.sync.fixed-delay-ms:5000}")
    public void sync() {
        syncUseCase.sync();
    }

    @Scheduled(fixedDelayString = "${app.state.cleanup-interval-ms:3600000}")
    public void cleanup() {
        maintenanceUseCase.cleanup();
    }
}
