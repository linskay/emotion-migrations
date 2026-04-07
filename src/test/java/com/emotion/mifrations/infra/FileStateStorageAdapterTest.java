package com.emotion.mifrations.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.model.PostMapping;
import com.emotion.mifrations.infrastructure.logging.InMemoryEventLogAdapter;
import com.emotion.mifrations.infrastructure.state.FileStateStorageAdapter;

class FileStateStorageAdapterTest {
    private Path file;

    @BeforeEach
    void setUp() throws Exception {
        file = Files.createTempFile("state", ".txt");
    }

    @Test
    void shouldPersistAndLoad() {
        FileStateStorageAdapter adapter = new FileStateStorageAdapter(props(), new InMemoryEventLogAdapter(props()));
        adapter.init();
        adapter.save(mapping(Instant.now()));
        assertTrue(adapter.find(1, 2).isPresent());
    }

    @Test
    void shouldPurgeOlderThan5Days() {
        FileStateStorageAdapter adapter = new FileStateStorageAdapter(props(), new InMemoryEventLogAdapter(props()));
        adapter.init();
        adapter.save(mapping(Instant.now().minus(6, ChronoUnit.DAYS)));
        adapter.purgeOlderThanDays(5);
        assertEquals(List.of(), adapter.findAll());
    }

    private PostMapping mapping(Instant updatedAt) {
        return PostMapping.builder()
                .telegramChannelId(1)
                .telegramMessageId(2)
                .vkCommunityId(3)
                .vkPostId(4)
                .lastEditVersion(1)
                .createdAt(Instant.now())
                .updatedAt(updatedAt)
                .build();
    }

    private AppProperties props() {
        return new AppProperties(
                new AppProperties.Telegram(new AppProperties.Telegram.Tdlib(1, "h", "+1"), List.of()),
                new AppProperties.Vk("t", "5.199", "url"),
                new AppProperties.State(file.toString(), 5),
                new AppProperties.Retry(3, "PT0.001S", 2),
                new AppProperties.Formatting("ЭмоциON"),
                new AppProperties.Notifications(false, "", ""),
                new AppProperties.Events(100));
    }
}
