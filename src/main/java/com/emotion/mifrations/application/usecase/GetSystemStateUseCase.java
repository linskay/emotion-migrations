package com.emotion.mifrations.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.emotion.mifrations.application.service.ChannelResolver;
import com.emotion.mifrations.domain.model.ChannelBinding;
import com.emotion.mifrations.domain.model.EventRecord;
import com.emotion.mifrations.domain.model.PostMapping;
import com.emotion.mifrations.domain.port.EventLogPort;
import com.emotion.mifrations.domain.port.StateStoragePort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetSystemStateUseCase {
    private final ChannelResolver resolver;
    private final EventLogPort eventLogPort;
    private final StateStoragePort stateStoragePort;

    public List<ChannelBinding> bindings() {
        return resolver.getBindings();
    }

    public List<EventRecord> recentEvents(int limit) {
        return eventLogPort.getRecent(limit);
    }

    public List<PostMapping> mappings() {
        return stateStoragePort.findAll();
    }
}
