package com.emotion.mifrations.domain.port;

import com.emotion.mifrations.domain.model.VkPostPayload;

public interface VkPublisherPort {
    long publish(long communityId, VkPostPayload payload);
    void edit(long communityId, long postId, VkPostPayload payload);
    String createPoll(long communityId, String question, java.util.List<String> options, boolean quiz, Integer correctOption);
}
