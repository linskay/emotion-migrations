package com.emotion.mifrations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Emotion Mifrations API")
                .description("Служебный API сервиса синхронизации постов Telegram -> VK")
                .version("1.0.0")
                .contact(new Contact().name("Backend Team")));
    }
}
