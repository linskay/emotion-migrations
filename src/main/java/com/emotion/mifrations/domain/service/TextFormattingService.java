package com.emotion.mifrations.domain.service;

import org.springframework.stereotype.Component;

@Component
public class TextFormattingService {

    public String normalizeForVk(String text, String sourceText, String sourceLink) {
        String base = text == null ? "" : text.trim();
        String sourceLine = "%s: %s".formatted(sourceText, sourceLink);
        if (base.isBlank()) {
            return sourceLine;
        }
        return base + System.lineSeparator() + System.lineSeparator() + sourceLine;
    }
}
