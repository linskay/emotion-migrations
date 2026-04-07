package com.emotion.mifrations.application.usecase;

import org.springframework.stereotype.Service;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.port.StateStoragePort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaintenanceUseCase {
    private final StateStoragePort stateStoragePort;
    private final AppProperties properties;

    public void cleanup() {
        stateStoragePort.purgeOlderThanDays(properties.state().retentionDays());
    }
}
