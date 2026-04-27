package com.emotion.mifrations.infrastructure.telegram.bot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.model.MediaType;
import com.emotion.mifrations.domain.model.PostStatus;
import com.emotion.mifrations.domain.model.TelegramMedia;
import com.emotion.mifrations.domain.model.TelegramPost;
import com.emotion.mifrations.domain.port.TelegramPostSourcePort;
import com.fasterxml.jackson.databind.JsonNode;

@Component
@Primary
public class BotApiTelegramSourceAdapter implements TelegramPostSourcePort {

    private final RestClient client = RestClient.create();
    private final AppProperties properties;
    private long offset = 0;

    public BotApiTelegramSourceAdapter(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<TelegramPost> fetchNewPosts() {
        String token = properties.telegram().botToken();
        if (token == null || token.isBlank()) {
            return List.of();
        }

        String url = "https://api.telegram.org/bot" + token + "/getUpdates?offset=" + offset;
        JsonNode response = client.get().uri(url).retrieve().body(JsonNode.class);

        List<TelegramPost> result = new ArrayList<>();

        for (JsonNode update : response.get("result")) {
            offset = update.get("update_id").asLong() + 1;

            JsonNode msg = update.has("channel_post") ? update.get("channel_post")
                    : update.get("edited_channel_post");

            if (msg == null) continue;

            long channelId = msg.get("chat").get("id").asLong();
            long messageId = msg.get("message_id").asLong();

            String text = msg.has("text") ? msg.get("text").asText()
                    : msg.has("caption") ? msg.get("caption").asText()
                    : "";

            List<TelegramMedia> media = new ArrayList<>();

            if (msg.has("photo")) {
                JsonNode photo = msg.get("photo").get(msg.get("photo").size() - 1);
                String fileId = photo.get("file_id").asText();
                String fileUrl = getFileUrl(token, fileId);
                media.add(TelegramMedia.builder()
                        .mediaId(fileId)
                        .type(MediaType.PHOTO)
                        .url(fileUrl)
                        .order(0)
                        .build());
            }

            if (msg.has("video")) {
                String fileId = msg.get("video").get("file_id").asText();
                String fileUrl = getFileUrl(token, fileId);
                media.add(TelegramMedia.builder()
                        .mediaId(fileId)
                        .type(MediaType.VIDEO)
                        .url(fileUrl)
                        .order(0)
                        .build());
            }

            result.add(TelegramPost.builder()
                    .channelId(channelId)
                    .messageId(messageId)
                    .text(text)
                    .media(media)
                    .status(update.has("edited_channel_post") ? PostStatus.EDITED : PostStatus.NEW)
                    .editVersion(msg.has("edit_date") ? msg.get("edit_date").asLong() : 0)
                    .publishedAt(Instant.ofEpochSecond(msg.get("date").asLong()))
                    .build());
        }

        return result;
    }

    private String getFileUrl(String token, String fileId) {
        String url = "https://api.telegram.org/bot" + token + "/getFile?file_id=" + fileId;
        JsonNode response = client.get().uri(url).retrieve().body(JsonNode.class);
        String path = response.get("result").get("file_path").asText();
        return "https://api.telegram.org/file/bot" + token + "/" + path;
    }
}
