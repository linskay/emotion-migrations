package com.emotion.mifrations.domain.port;

import java.util.List;
import java.util.Optional;

import com.emotion.mifrations.domain.model.PostMapping;

public interface StateStoragePort {
    Optional<PostMapping> find(long telegramChannelId, long telegramMessageId);
    void save(PostMapping mapping);
    List<PostMapping> findAll();
    void purgeOlderThanDays(int days);
}
