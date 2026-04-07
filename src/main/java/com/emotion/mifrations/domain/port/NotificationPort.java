package com.emotion.mifrations.domain.port;

public interface NotificationPort {
    void notifyInfo(String message);
    void notifyError(String message);
}
