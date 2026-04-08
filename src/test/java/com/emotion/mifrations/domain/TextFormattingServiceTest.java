package com.emotion.mifrations.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.emotion.mifrations.domain.service.TextFormattingService;

class TextFormattingServiceTest {
    @Test
    void shouldAppendSourceLink() {
        String value = new TextFormattingService().normalizeForVk("Привет\nмир", "ЭмоциON", "https://t.me/a/1");
        assertTrue(value.contains("ЭмоциON: https://t.me/a/1"));
    }
}
