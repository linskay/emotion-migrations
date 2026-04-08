package com.emotion.mifrations.domain.port;

import java.util.List;

import com.emotion.mifrations.domain.model.VkPostPayload;

public interface VkPublisherPort {
    long publish(long communityId, VkPostPayload payload);

    void edit(long communityId, long postId, VkPostPayload payload);

    String createPoll(long communityId, String question, List<String> options, boolean quiz, Integer correctOption);

    String uploadPhoto(long communityId, String sourceUrl);

    String uploadVideo(long communityId, String sourceUrl);
}
