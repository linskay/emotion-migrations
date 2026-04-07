package com.emotion.mifrations.infrastructure.state;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.exception.StateStorageException;
import com.emotion.mifrations.domain.model.PostMapping;
import com.emotion.mifrations.domain.port.EventLogPort;
import com.emotion.mifrations.domain.port.StateStoragePort;

import jakarta.annotation.PostConstruct;

@Component
public class FileStateStorageAdapter implements StateStoragePort {
    private final Path filePath;
    private final EventLogPort eventLogPort;
    private final Map<String, PostMapping> cache = new HashMap<>();

    public FileStateStorageAdapter(AppProperties properties, EventLogPort eventLogPort) {
        this.filePath = Path.of(properties.state().filePath());
        this.eventLogPort = eventLogPort;
    }

    @PostConstruct
    public void init() {
        load();
    }

    @Override
    public synchronized Optional<PostMapping> find(long telegramChannelId, long telegramMessageId) {
        return Optional.ofNullable(cache.get(key(telegramChannelId, telegramMessageId)));
    }

    @Override
    public synchronized void save(PostMapping mapping) {
        cache.put(key(mapping.telegramChannelId(), mapping.telegramMessageId()), mapping);
        persist();
    }

    @Override
    public synchronized List<PostMapping> findAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public synchronized void purgeOlderThanDays(int days) {
        Instant threshold = Instant.now().minus(days, ChronoUnit.DAYS);
        cache.entrySet().removeIf(entry -> entry.getValue().updatedAt().isBefore(threshold));
        persist();
        eventLogPort.info("STATE_PURGED", "Выполнена очистка устаревшего состояния");
    }

    private void load() {
        try {
            if (!Files.exists(filePath)) {
                Path parent = filePath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.createFile(filePath);
                return;
            }
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\|");
                PostMapping mapping = PostMapping.builder()
                        .telegramChannelId(Long.parseLong(parts[0]))
                        .telegramMessageId(Long.parseLong(parts[1]))
                        .vkCommunityId(Long.parseLong(parts[2]))
                        .vkPostId(Long.parseLong(parts[3]))
                        .lastEditVersion(Long.parseLong(parts[4]))
                        .createdAt(Instant.parse(parts[5]))
                        .updatedAt(Instant.parse(parts[6]))
                        .build();
                cache.put(key(mapping.telegramChannelId(), mapping.telegramMessageId()), mapping);
            }
        } catch (IOException ex) {
            throw new StateStorageException("Не удалось загрузить состояние", ex);
        }
    }

    private void persist() {
        try {
            List<String> lines = cache.values().stream()
                    .map(v -> String.join("|",
                            Long.toString(v.telegramChannelId()),
                            Long.toString(v.telegramMessageId()),
                            Long.toString(v.vkCommunityId()),
                            Long.toString(v.vkPostId()),
                            Long.toString(v.lastEditVersion()),
                            v.createdAt().toString(),
                            v.updatedAt().toString()))
                    .toList();
            Files.write(filePath, lines);
        } catch (IOException ex) {
            throw new StateStorageException("Не удалось сохранить состояние", ex);
        }
    }

    private String key(long channelId, long messageId) {
        return channelId + ":" + messageId;
    }
}
