package com.emotion.mifrations.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.port.EventLogPort;
import static org.mockito.Mockito.mock;
import com.emotion.mifrations.infrastructure.retry.RetryExecutor;

class RetryExecutorTest {

    @Test
    void shouldRetryAndSucceed() {
        AppProperties props = new AppProperties(
                new AppProperties.Telegram(new AppProperties.Telegram.Tdlib(1, "h", "+1", "build/tdlib-db", "build/tdlib-files", 100), List.of()),
                new AppProperties.Vk("t", "5.199", "https://api.vk.com/method", "ua", "build/tmp-media"),
                new AppProperties.State("build/test-state.txt", 5),
                new AppProperties.Retry(3, "PT0.001S", 2),
                new AppProperties.Formatting("ЭмоциON"),
                new AppProperties.Notifications(false, "", ""),
                new AppProperties.Events(100, "build/events", "events.log", 4096, 3));
        RetryExecutor retry = new RetryExecutor(props, mock(EventLogPort.class));
        AtomicInteger attempts = new AtomicInteger();
        Integer value = retry.execute("test", () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("fail");
            }
            return 42;
        });
        assertEquals(42, value);
        assertEquals(3, attempts.get());
    }
}
