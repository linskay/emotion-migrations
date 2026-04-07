package com.emotion.mifrations.domain.model;

import java.util.List;

import lombok.Builder;

@Builder
public record TelegramPoll(String question, List<String> options, boolean quiz, Integer correctOption) {
}
