package com.emotion.mifrations.usecase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.emotion.mifrations.application.service.ChannelResolver;
import com.emotion.mifrations.application.usecase.ProcessTelegramPostUseCase;
import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.exception.DuplicatePostException;
import com.emotion.mifrations.domain.model.MediaType;
import com.emotion.mifrations.domain.model.PostMapping;
import com.emotion.mifrations.domain.model.PostStatus;
import com.emotion.mifrations.domain.model.TelegramMedia;
import com.emotion.mifrations.domain.model.TelegramPoll;
import com.emotion.mifrations.domain.model.TelegramPost;
import com.emotion.mifrations.domain.port.EventLogPort;
import com.emotion.mifrations.domain.port.NotificationPort;
import com.emotion.mifrations.domain.port.StateStoragePort;
import com.emotion.mifrations.domain.port.VkPublisherPort;
import com.emotion.mifrations.domain.service.TextFormattingService;
import com.emotion.mifrations.infrastructure.retry.RetryExecutor;

class ProcessTelegramPostUseCaseTest {

    private StateStoragePort stateStorage;
    private VkPublisherPort vkPublisher;
    private NotificationPort notification;
    private EventLogPort eventLog;
    private ProcessTelegramPostUseCase useCase;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties(
                new AppProperties.Telegram(new AppProperties.Telegram.Tdlib(1, "h", "+1"),
                        List.of(new AppProperties.Telegram.Channel(10, "tg", 20, "vk"))),
                new AppProperties.Vk("t", "5.199", "url"),
                new AppProperties.State("build/test-state.txt", 5),
                new AppProperties.Retry(3, "PT0.001S", 2),
                new AppProperties.Formatting("ЭмоциON"),
                new AppProperties.Notifications(false, "", ""),
                new AppProperties.Events(100));
        stateStorage = Mockito.mock(StateStoragePort.class);
        vkPublisher = Mockito.mock(VkPublisherPort.class);
        notification = Mockito.mock(NotificationPort.class);
        eventLog = Mockito.mock(EventLogPort.class);
        useCase = new ProcessTelegramPostUseCase(new ChannelResolver(props), stateStorage, vkPublisher,
                notification, eventLog, new TextFormattingService(), new RetryExecutor(props, eventLog), props);
    }

    @Test
    void shouldSkipUnsupportedContent() {
        TelegramPost post = TelegramPost.builder()
                .channelId(10)
                .messageId(1)
                .status(PostStatus.NEW)
                .sourceLink("link")
                .media(List.of(TelegramMedia.builder().mediaId("1").type(MediaType.AUDIO).url("u").build()))
                .editVersion(1)
                .build();
        assertFalse(useCase.process(post).published());
    }

    @Test
    void shouldThrowDuplicate() {
        when(stateStorage.find(10, 1)).thenReturn(Optional.of(existing()));
        TelegramPost post = TelegramPost.builder().channelId(10).messageId(1).status(PostStatus.NEW).sourceLink("l").editVersion(1).build();
        assertThrows(DuplicatePostException.class, () -> useCase.process(post));
    }

    @Test
    void shouldPublishPoll() {
        when(stateStorage.find(10, 2)).thenReturn(Optional.empty());
        when(vkPublisher.createPoll(any(), any(), any(), any(), any())).thenReturn("poll1");
        when(vkPublisher.publish(any(), any())).thenReturn(100L);
        doNothing().when(stateStorage).save(any());
        TelegramPost post = TelegramPost.builder().channelId(10).messageId(2).status(PostStatus.NEW).sourceLink("l")
                .poll(TelegramPoll.builder().question("Q?").options(List.of("a", "b")).quiz(false).build())
                .editVersion(1).build();
        useCase.process(post);
        verify(vkPublisher).publish(any(), any());
    }

    @Test
    void shouldUpdateEditedPost() {
        when(stateStorage.find(10, 3)).thenReturn(Optional.of(existing()));
        TelegramPost post = TelegramPost.builder().channelId(10).messageId(3).status(PostStatus.EDITED).sourceLink("l").editVersion(2).build();
        useCase.process(post);
        verify(vkPublisher).edit(any(), any(), any());
    }

    private PostMapping existing() {
        return PostMapping.builder().telegramChannelId(10).telegramMessageId(3).vkCommunityId(20).vkPostId(300)
                .lastEditVersion(1).createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
