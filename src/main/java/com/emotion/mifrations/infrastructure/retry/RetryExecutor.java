package com.emotion.mifrations.infrastructure.retry;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.exception.TelegramIntegrationException;
import com.emotion.mifrations.domain.port.EventLogPort;

@Component
public class RetryExecutor {
    private final AppProperties.Retry config;
    private final EventLogPort eventLogPort;

    public RetryExecutor(AppProperties properties, EventLogPort eventLogPort) {
        this.config = properties.retry();
        this.eventLogPort = eventLogPort;
    }

    public <T> T execute(String operation, Supplier<T> supplier) {
        int attempt = 0;
        Duration delay = config.initialBackoffDuration();
        while (true) {
            attempt++;
            try {
                return supplier.get();
            } catch (RuntimeException ex) {
                if (attempt >= config.maxAttempts()) {
                    eventLogPort.error("RETRY_FAILED", "Операция %s провалилась после %d попыток".formatted(operation, attempt));
                    throw ex;
                }
                sleep(delay);
                eventLogPort.warn("RETRY_ATTEMPT", "Повтор %s, попытка %d".formatted(operation, attempt + 1));
                delay = Duration.ofMillis((long) (delay.toMillis() * config.multiplier()));
            }
        }
    }

    public void executeVoid(String operation, Runnable action) {
        execute(operation, () -> {
            action.run();
            return Boolean.TRUE;
        });
    }

    private void sleep(Duration delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TelegramIntegrationException("Поток retry был прерван", e);
        }
    }
}
