package com.emotion.mifrations.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.exception.NotificationDeliveryException;
import com.emotion.mifrations.domain.port.NotificationPort;

@Component
public class LoggingNotificationAdapter implements NotificationPort {
    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationAdapter.class);

    private final AppProperties.Notifications notifications;

    public LoggingNotificationAdapter(AppProperties properties) {
        this.notifications = properties.notifications();
    }

    @Override
    public void notifyInfo(String message) {
        if (!notifications.enabled()) {
            return;
        }
        if (notifications.targetChatId() == null || notifications.targetChatId().isBlank()) {
            throw new NotificationDeliveryException("Не задан target-chat-id для уведомлений");
        }
        log.info("Уведомление INFO отправлено: {}", message);
    }

    @Override
    public void notifyError(String message) {
        if (!notifications.enabled()) {
            return;
        }
        if (notifications.targetChatId() == null || notifications.targetChatId().isBlank()) {
            throw new NotificationDeliveryException("Не задан target-chat-id для уведомлений");
        }
        log.error("Уведомление ERROR отправлено: {}", message);
    }
}
