package com.emotion.mifrations.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.emotion.mifrations.application.dto.PublishResult;
import com.emotion.mifrations.application.service.ChannelResolver;
import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.exception.DuplicatePostException;
import com.emotion.mifrations.domain.exception.MediaDownloadException;
import com.emotion.mifrations.domain.exception.PostAssemblyException;
import com.emotion.mifrations.domain.exception.PostUpdateException;
import com.emotion.mifrations.domain.model.ChannelBinding;
import com.emotion.mifrations.domain.model.PostMapping;
import com.emotion.mifrations.domain.model.PostStatus;
import com.emotion.mifrations.domain.model.TelegramMedia;
import com.emotion.mifrations.domain.model.TelegramPost;
import com.emotion.mifrations.domain.model.VkPostPayload;
import com.emotion.mifrations.domain.port.EventLogPort;
import com.emotion.mifrations.domain.port.NotificationPort;
import com.emotion.mifrations.domain.port.StateStoragePort;
import com.emotion.mifrations.domain.port.VkPublisherPort;
import com.emotion.mifrations.domain.service.TextFormattingService;
import com.emotion.mifrations.infrastructure.retry.RetryExecutor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessTelegramPostUseCase {
    private final ChannelResolver channelResolver;
    private final StateStoragePort stateStorage;
    private final VkPublisherPort vkPublisher;
    private final NotificationPort notifier;
    private final EventLogPort eventLog;
    private final TextFormattingService formatter;
    private final RetryExecutor retryExecutor;
    private final AppProperties properties;

    public PublishResult process(TelegramPost post) {
        ChannelBinding binding = channelResolver.resolve(post.channelId());
        if (post.hasUnsupportedMedia()) {
            String message = "Пост пропущен: неподдерживаемый тип медиа для сообщения %d".formatted(post.messageId());
            eventLog.warn("UNSUPPORTED_CONTENT", message);
            notifier.notifyError(message);
            return PublishResult.builder().published(false).reason(message).build();
        }

        var existing = stateStorage.find(post.channelId(), post.messageId());
        if (existing.isPresent() && post.status() == PostStatus.NEW) {
            throw new DuplicatePostException("Пост уже был обработан: " + post.messageId());
        }

        VkPostPayload payload = assemblePayload(post);
        if (existing.isPresent() || post.status() == PostStatus.EDITED) {
            return update(post, binding, existing.orElseThrow(() -> new PostUpdateException("Не найдена запись состояния для обновления поста")), payload);
        }

        long vkPostId = retryExecutor.execute("vk-publish", () -> vkPublisher.publish(binding.vkCommunityId(), payload));
        stateStorage.save(PostMapping.builder()
                .telegramChannelId(post.channelId())
                .telegramMessageId(post.messageId())
                .vkCommunityId(binding.vkCommunityId())
                .vkPostId(vkPostId)
                .lastEditVersion(post.editVersion())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
        String msg = "Успешно опубликован пост TG %d в VK %d".formatted(post.messageId(), vkPostId);
        eventLog.info("POST_PUBLISHED", msg);
        notifier.notifyInfo(msg);
        return PublishResult.builder().published(true).vkPostId(vkPostId).reason("Опубликовано").build();
    }

    private PublishResult update(TelegramPost post, ChannelBinding binding, PostMapping existing, VkPostPayload payload) {
        if (post.editVersion() <= existing.lastEditVersion()) {
            String reason = "Обновление не требуется: версия редактирования не изменилась";
            eventLog.info("POST_SKIP_NO_CHANGES", reason);
            return PublishResult.builder().published(false).reason(reason).vkPostId(existing.vkPostId()).build();
        }
        retryExecutor.executeVoid("vk-edit", () -> vkPublisher.edit(binding.vkCommunityId(), existing.vkPostId(), payload));
        stateStorage.save(PostMapping.builder()
                .telegramChannelId(post.channelId())
                .telegramMessageId(post.messageId())
                .vkCommunityId(binding.vkCommunityId())
                .vkPostId(existing.vkPostId())
                .lastEditVersion(post.editVersion())
                .createdAt(existing.createdAt())
                .updatedAt(Instant.now())
                .build());
        String msg = "Пост обновлен TG %d -> VK %d".formatted(post.messageId(), existing.vkPostId());
        eventLog.info("POST_UPDATED", msg);
        notifier.notifyInfo(msg);
        return PublishResult.builder().published(true).reason("Обновлено").vkPostId(existing.vkPostId()).build();
    }

    private VkPostPayload assemblePayload(TelegramPost post) {
        try {
            List<String> attachments = new ArrayList<>();
            if (post.media() != null) {
                for (TelegramMedia media : post.media()) {
                    if (media.type().name().equals("PHOTO") || media.type().name().equals("VIDEO")) {
                        String attachment = retryExecutor.execute("media-transfer", () -> transferMedia(post.channelId(), media));
                        attachments.add(attachment);
                    }
                }
            }
            String pollAttachment = null;
            if (post.poll() != null) {
                pollAttachment = retryExecutor.execute("poll-create", () -> vkPublisher.createPoll(
                        channelResolver.resolve(post.channelId()).vkCommunityId(),
                        post.poll().question(),
                        post.poll().options(),
                        post.poll().quiz(),
                        post.poll().correctOption()));
            }
            String finalText = formatter.normalizeForVk(post.text(), properties.formatting().sourceLinkText(), post.sourceLink());
            return VkPostPayload.builder().text(finalText).attachments(attachments).pollAttachment(pollAttachment).build();
        } catch (Exception ex) {
            if (ex instanceof MediaDownloadException) {
                throw ex;
            }
            throw new PostAssemblyException("Не удалось собрать полезную нагрузку поста", ex);
        }
    }

    private String transferMedia(long channelId, TelegramMedia media) {
        if (media.url() == null || media.url().isBlank()) {
            throw new MediaDownloadException("Отсутствует URL медиа: " + media.mediaId());
        }
        return switch (media.type()) {
            case PHOTO -> vkPublisher.uploadPhoto(channelResolver.resolve(channelId).vkCommunityId(), media.url());
            case VIDEO -> vkPublisher.uploadVideo(channelResolver.resolve(channelId).vkCommunityId(), media.url());
            default -> throw new MediaDownloadException("Неподдерживаемый тип медиа для публикации: " + media.type());
        };
    }
}
