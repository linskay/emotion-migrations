package com.emotion.mifrations.infrastructure.telegram.tdlib;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.emotion.mifrations.domain.model.TelegramPost;
import com.emotion.mifrations.domain.port.TelegramPostSourcePort;

@Component
public class TdlibTelegramSourceAdapter implements TelegramPostSourcePort {
    private final List<TelegramPost> queue = new CopyOnWriteArrayList<>();

    @Override
    public List<TelegramPost> fetchNewPosts() {
        List<TelegramPost> copy = List.copyOf(queue);
        queue.clear();
        return copy;
    }

    public void ingest(TelegramPost post) {
        queue.add(post);
    }
}
