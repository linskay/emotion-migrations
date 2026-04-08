package com.emotion.mifrations.infrastructure.vk;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.exception.MediaDownloadException;
import com.emotion.mifrations.domain.exception.PollCreationException;
import com.emotion.mifrations.domain.exception.VkIntegrationException;
import com.emotion.mifrations.domain.model.VkPostPayload;
import com.emotion.mifrations.domain.port.VkPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VkPublisherAdapter implements VkPublisherPort {
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {};

    private final RestClient vkClient;
    private final RestClient uploadClient;
    private final ObjectMapper objectMapper;
    private final AppProperties properties;

    public VkPublisherAdapter(AppProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.vkClient = RestClient.builder().baseUrl(properties.vk().apiUrl()).build();
        this.uploadClient = RestClient.builder().defaultHeader("User-Agent", properties.vk().uploadUserAgent()).build();
        initTempDir();
    }

    @Override
    public long publish(long communityId, VkPostPayload payload) {
        Map<String, String> query = authParams();
        query.put("owner_id", "-" + communityId);
        query.put("message", payload.text() == null ? "" : payload.text());
        query.put("attachments", attachmentString(payload));
        Map<String, Object> response = callVkMethod("wall.post", query);
        return responseNumber(response, "post_id").longValue();
    }

    @Override
    public void edit(long communityId, long postId, VkPostPayload payload) {
        Map<String, String> query = authParams();
        query.put("owner_id", "-" + communityId);
        query.put("post_id", Long.toString(postId));
        query.put("message", payload.text() == null ? "" : payload.text());
        query.put("attachments", attachmentString(payload));
        callVkMethod("wall.edit", query);
    }

    @Override
    public String createPoll(long communityId, String question, List<String> options, boolean quiz, Integer correctOption) {
        if (question == null || question.isBlank() || options == null || options.size() < 2) {
            throw new PollCreationException("Некорректные данные для создания опроса VK");
        }
        Map<String, String> query = authParams();
        query.put("owner_id", "-" + communityId);
        query.put("question", question);
        query.put("is_quiz", quiz ? "1" : "0");
        query.put("add_answers", toJson(options));
        if (quiz && correctOption != null) {
            query.put("correct_answer_id", Integer.toString(correctOption + 1));
        }
        Map<String, Object> response = callVkMethod("polls.create", query);
        Number pollId = responseNumber(response, "id");
        return "poll-" + communityId + "_" + pollId.longValue();
    }

    @Override
    public String uploadPhoto(long communityId, String sourceUrl) {
        Path file = downloadTemp(sourceUrl, "photo");
        try {
            String uploadUrl = (String) callVkMethod("photos.getWallUploadServer", authParams()).get("upload_url");
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("photo", new FileSystemResource(file));
            Map<String, Object> uploadResult = uploadClient.post().uri(URI.create(uploadUrl))
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve().body(MAP_TYPE);

            Map<String, String> query = authParams();
            query.put("server", String.valueOf(uploadResult.get("server")));
            query.put("photo", String.valueOf(uploadResult.get("photo")));
            query.put("hash", String.valueOf(uploadResult.get("hash")));
            List<Map<String, Object>> saved = castList(callVkMethod("photos.saveWallPhoto", query));
            Map<String, Object> first = saved.getFirst();
            return "photo" + first.get("owner_id") + "_" + first.get("id");
        } finally {
            deleteQuietly(file);
        }
    }

    @Override
    public String uploadVideo(long communityId, String sourceUrl) {
        Path file = downloadTemp(sourceUrl, "video");
        try {
            Map<String, String> query = authParams();
            query.put("group_id", Long.toString(communityId));
            query.put("name", file.getFileName().toString());
            Map<String, Object> prepare = callVkMethod("video.save", query);
            String uploadUrl = (String) prepare.get("upload_url");

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("video_file", new FileSystemResource(file));
            uploadClient.post().uri(URI.create(uploadUrl))
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve().toBodilessEntity();

            return "video" + prepare.get("owner_id") + "_" + prepare.get("video_id");
        } finally {
            deleteQuietly(file);
        }
    }

    private String attachmentString(VkPostPayload payload) {
        String media = payload.attachments() == null ? "" : String.join(",", payload.attachments());
        if (payload.pollAttachment() == null || payload.pollAttachment().isBlank()) {
            return media;
        }
        if (media.isBlank()) {
            return payload.pollAttachment();
        }
        return media + "," + payload.pollAttachment();
    }

    private Map<String, Object> callVkMethod(String method, Map<String, String> query) {
        Map<String, Object> body = vkClient.get().uri(uriBuilder -> {
            uriBuilder.path("/" + method);
            query.forEach(uriBuilder::queryParam);
            return uriBuilder.build();
        }).retrieve().body(MAP_TYPE);

        Object error = body.get("error");
        if (error != null) {
            throw new VkIntegrationException("VK API вернул ошибку для метода %s: %s".formatted(method, error));
        }
        Object response = body.get("response");
        if (response instanceof Map<?, ?> responseMap) {
            return castMap(responseMap);
        }
        if (response instanceof List<?> list) {
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("list", list);
            return wrapper;
        }
        throw new VkIntegrationException("Некорректный ответ VK API для метода: " + method);
    }

    private Map<String, String> authParams() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", properties.vk().token());
        params.put("v", properties.vk().apiVersion());
        return params;
    }

    private Number responseNumber(Map<String, Object> response, String key) {
        Object value = response.get(key);
        if (value instanceof Number n) {
            return n;
        }
        throw new VkIntegrationException("В ответе VK отсутствует поле: " + key);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Map<String, Object> wrapper) {
        Object data = wrapper.get("list");
        if (!(data instanceof List<?> list)) {
            throw new VkIntegrationException("Некорректный list-ответ VK API");
        }
        return (List<Map<String, Object>>) list;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new PollCreationException("Не удалось сериализовать варианты опроса", e);
        }
    }

    private Path downloadTemp(String sourceUrl, String prefix) {
        try {
            Path dir = Path.of(properties.vk().mediaTempDir());
            Files.createDirectories(dir);
            Path target = Files.createTempFile(dir, prefix + "-", ".bin");
            byte[] content = uploadClient.get().uri(sourceUrl).retrieve().body(byte[].class);
            Files.write(target, content);
            return target;
        } catch (IOException | RuntimeException ex) {
            throw new MediaDownloadException("Не удалось скачать медиа из Telegram: " + sourceUrl, ex);
        }
    }

    private void initTempDir() {
        try {
            Files.createDirectories(Path.of(properties.vk().mediaTempDir()));
        } catch (IOException ex) {
            throw new VkIntegrationException("Не удалось создать временную директорию VK media", ex);
        }
    }

    private void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }
    }
}
