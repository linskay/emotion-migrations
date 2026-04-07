package com.emotion.mifrations.domain.model;

import lombok.Builder;

@Builder
public record ChannelBinding(long telegramChannelId, String telegramName, long vkCommunityId, String vkName) {
}
