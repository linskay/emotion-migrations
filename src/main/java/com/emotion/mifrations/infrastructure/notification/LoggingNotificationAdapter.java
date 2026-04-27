package com.emotion.mifrations.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.exception.NotificationDeliveryException;
import com.emotion.mifrations.domain.port.NotificationPort;

@Component
public class LoggingNotificationAdapter implements NotificationPort {
    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationAdapter.class);

    private final RestClient client = RestClient.create();
    private final AppProperties.Notifications notifications;

    public LoggingNotificationAdapter(AppProperties properties) {
        this.notifications = properties.notifications();
    }

    @Override
    public void notifyInfo(String message) {
        send("ℹ️ INFO", message);
    }

    @Override
    public void notifyError(String message) {
        send("🚨 ERROR", message);
    }

    private void send(String level, String message) {
        if (!notifications.enabled()) {
            return;
        }
        if (notifications.botToken() == null || notifications.botToken().isBlank()) {
            throw new NotificationDeliveryException("Не задан bot-token для уведомлений");
        }
        if (notifications.targetChatId() == null || notifications.targetChatId().isBlank()) {
            throw new NotificationDeliveryException("Не задан target-chat-id для уведомлений");
        }

        String text = level + "\n" + message;
        String url = "https://api.telegram.org/bot" + notifications.botToken() + "/sendMessage";

        try {
            client.post()
                    .uri(url)
                    .body(new SendMessageRequest(notifications.targetChatId(), text))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Telegram notification sent: {}", text);
        } catch (Exception ex) {
            log.error("Failed to send Telegram notification", ex);
            throw new NotificationDeliveryException("Не удалось отправить Telegram-уведомление", ex);
        }
    }

    private record SendMessageRequest(String chat_id, String text) {
    }
}
