package com.emotion.mifrations.domain.port;

import java.util.List;

import com.emotion.mifrations.domain.model.EventRecord;

public interface EventLogPort {
    void info(String code, String message);
    void warn(String code, String message);
    void error(String code, String message);
    List<EventRecord> getRecent(int limit);
}
