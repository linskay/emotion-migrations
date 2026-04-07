package com.emotion.mifrations.domain.model;

import java.time.Instant;

import lombok.Builder;

@Builder
public record EventRecord(Instant time, String level, String code, String message, String correlationId) {
}
