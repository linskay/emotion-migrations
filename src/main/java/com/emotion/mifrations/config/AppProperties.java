package com.emotion.mifrations.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(@Valid Telegram telegram,
                            @Valid Vk vk,
                            @Valid State state,
                            @Valid Retry retry,
                            @Valid Formatting formatting,
                            @Valid Notifications notifications,
                            @Valid Events events) {

    public record Telegram(String botToken, @Valid Tdlib tdlib, @NotEmpty List<@Valid Channel> channels) {
        public record Tdlib(@Min(1) int apiId,
                            @NotBlank String apiHash,
                            @NotBlank String phoneNumber,
                            @NotBlank String databaseDirectory,
                            @NotBlank String filesDirectory,
                            @Min(100) int fetchBatchSize) {
        }

        public record Channel(long sourceId, @NotBlank String sourceName, long vkCommunityId, @NotBlank String vkCommunityName) {
        }
    }

    public record Vk(@NotBlank String token,
                     @NotBlank String apiVersion,
                     @NotBlank String apiUrl,
                     @NotBlank String uploadUserAgent,
                     @NotBlank String mediaTempDir) {
    }

    public record State(@NotBlank String filePath, @Min(1) int retentionDays) {
    }

    public record Retry(@Min(1) int maxAttempts, @NotBlank String initialBackoff, double multiplier) {
        public Duration initialBackoffDuration() {
            return Duration.parse(initialBackoff);
        }
    }

    public record Formatting(@NotBlank String sourceLinkText) {
    }

    public record Notifications(boolean enabled, String botToken, String targetChatId) {
    }

    public record Events(@Min(10) int maxInMemoryEvents,
                         @NotBlank String journalDir,
                         @NotBlank String journalFileName,
                         @Min(1024) int journalMaxSizeBytes,
                         @Min(1) int journalMaxFiles) {
    }
}
