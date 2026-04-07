package com.emotion.mifrations.infrastructure.vk;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.emotion.mifrations.domain.exception.PollCreationException;
import com.emotion.mifrations.domain.exception.VkIntegrationException;
import com.emotion.mifrations.domain.model.VkPostPayload;
import com.emotion.mifrations.domain.port.VkPublisherPort;

@Component
public class VkPublisherAdapter implements VkPublisherPort {
    private final AtomicLong generatedId = new AtomicLong(1000);

    @Override
    public long publish(long communityId, VkPostPayload payload) {
        if (payload == null) {
            throw new VkIntegrationException("Пустой payload для публикации VK");
        }
        return generatedId.incrementAndGet();
    }

    @Override
    public void edit(long communityId, long postId, VkPostPayload payload) {
        if (postId <= 0) {
            throw new VkIntegrationException("Некорректный id поста VK для редактирования");
        }
    }

    @Override
    public String createPoll(long communityId, String question, List<String> options, boolean quiz, Integer correctOption) {
        if (question == null || question.isBlank() || options == null || options.size() < 2) {
            throw new PollCreationException("Некорректные данные для создания опроса VK");
        }
        if (quiz && (correctOption == null || correctOption < 0 || correctOption >= options.size())) {
            throw new PollCreationException("Для викторины требуется корректный индекс правильного ответа");
        }
        return "poll" + communityId + "_" + generatedId.incrementAndGet();
    }
}
