package com.emotion.mifrations.infrastructure.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.exception.StateStorageException;
import com.emotion.mifrations.domain.model.EventRecord;
import com.emotion.mifrations.domain.port.EventLogPort;

import jakarta.annotation.PostConstruct;

@Component
public class FileBackedRollingEventLogAdapter implements EventLogPort {
    private final Deque<EventRecord> memory;
    private final int maxSize;
    private final Path journalDir;
    private final String fileName;
    private final long maxBytes;
    private final int maxFiles;

    public FileBackedRollingEventLogAdapter(AppProperties properties) {
        this.maxSize = properties.events().maxInMemoryEvents();
        this.memory = new ArrayDeque<>(maxSize);
        this.journalDir = Path.of(properties.events().journalDir());
        this.fileName = properties.events().journalFileName();
        this.maxBytes = properties.events().journalMaxSizeBytes();
        this.maxFiles = properties.events().journalMaxFiles();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(journalDir);
            Path active = activeFile();
            if (!Files.exists(active)) {
                Files.createFile(active);
            }
        } catch (IOException ex) {
            throw new StateStorageException("Не удалось инициализировать event-journal", ex);
        }
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
        return memory.stream().limit(limit).toList();
    }

    private void push(String level, String code, String message) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        EventRecord event = EventRecord.builder()
                .time(Instant.now())
                .level(level)
                .code(code)
                .message(message)
                .correlationId(correlationId)
                .build();
        if (memory.size() >= maxSize) {
            memory.removeLast();
        }
        memory.addFirst(event);
        append(event);
    }

    private void append(EventRecord event) {
        rotateIfNeeded();
        String line = toJsonLine(event) + System.lineSeparator();
        try {
            Files.writeString(activeFile(), line, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            throw new StateStorageException("Не удалось записать событие в event-journal", ex);
        }
    }

    private void rotateIfNeeded() {
        try {
            Path active = activeFile();
            if (!Files.exists(active) || Files.size(active) < maxBytes) {
                return;
            }
            for (int i = maxFiles - 1; i >= 1; i--) {
                Path current = journalDir.resolve(fileName + "." + i);
                if (Files.exists(current)) {
                    if (i == maxFiles - 1) {
                        Files.delete(current);
                    } else {
                        Files.move(current, journalDir.resolve(fileName + "." + (i + 1)));
                    }
                }
            }
            Files.move(active, journalDir.resolve(fileName + ".1"));
            Files.createFile(active);
        } catch (IOException ex) {
            throw new StateStorageException("Не удалось выполнить ротацию event-journal", ex);
        }
    }

    private String toJsonLine(EventRecord event) {
        return "{\"time\":\"%s\",\"level\":\"%s\",\"code\":\"%s\",\"message\":\"%s\",\"correlationId\":\"%s\"}"
                .formatted(event.time(), escape(event.level()), escape(event.code()), escape(event.message()), escape(event.correlationId()));
    }

    private String escape(String source) {
        return source.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Path activeFile() {
        return journalDir.resolve(fileName);
    }
}
