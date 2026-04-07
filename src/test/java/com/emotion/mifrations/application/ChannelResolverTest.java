package com.emotion.mifrations.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.emotion.mifrations.application.service.ChannelResolver;
import com.emotion.mifrations.config.AppProperties;
import com.emotion.mifrations.domain.exception.ConfigurationException;

class ChannelResolverTest {

    @Test
    void shouldResolveMapping() {
        ChannelResolver resolver = new ChannelResolver(props());
        assertEquals(20L, resolver.resolve(10L).vkCommunityId());
    }

    @Test
    void shouldThrowWhenNotFound() {
        ChannelResolver resolver = new ChannelResolver(props());
        assertThrows(ConfigurationException.class, () -> resolver.resolve(999));
    }

    private AppProperties props() {
        return new AppProperties(
                new AppProperties.Telegram(new AppProperties.Telegram.Tdlib(1, "h", "+1"),
                        List.of(new AppProperties.Telegram.Channel(10, "tg", 20, "vk"))),
                new AppProperties.Vk("t", "5.199", "url"),
                new AppProperties.State("build/test-state.txt", 5),
                new AppProperties.Retry(3, "PT0.001S", 2),
                new AppProperties.Formatting("ЭмоциON"),
                new AppProperties.Notifications(false, "", ""),
                new AppProperties.Events(100));
    }
}
