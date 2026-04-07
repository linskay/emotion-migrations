package com.emotion.mifrations.domain.port;

import java.util.List;

import com.emotion.mifrations.domain.model.TelegramPost;

public interface TelegramPostSourcePort {
    List<TelegramPost> fetchNewPosts();
}
