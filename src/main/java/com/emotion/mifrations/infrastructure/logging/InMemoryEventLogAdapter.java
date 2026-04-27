package com.emotion.mifrations.infrastructure.logging;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.model.EventRecord;
import com.emotion.mifrations.domain.port.EventLogPort;

@Component
public class InMemoryEventLogAdapter implements EventLogPort {
    private final Deque<EventRecord> events = new ArrayDeque<>();
    private final int maxSize;

    public InMemoryEventLogAdapter(AppProperties properties) {
        this.maxSize = properties.events().maxInMemoryEvents();
    }

    @Override
    public synchronized void info(String code, String message) {
        push("INFO", code, message);
    }

    @Override
    public synchronized void warn(String code, String message) {
        push("WARN", code, message);
    }

    @Override
    public synchronized void error(String code, String message) {
        push("ERROR", code, message);
    }

    @Override
    public synchronized List<EventRecord> getRecent(int limit) {
        return events.stream().limit(limit).toList();
    }

    private void push(String level, String code, String message) {
        if (events.size() >= maxSize) {
            events.removeLast();
        }

        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        events.addFirst(EventRecord.builder()
                .time(Instant.now())
                .level(level)
                .code(code)
                .message(message)
                .correlationId(correlationId)
                .build());
    }
}
