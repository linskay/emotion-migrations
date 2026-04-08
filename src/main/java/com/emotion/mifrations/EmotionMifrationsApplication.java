package com.emotion.mifrations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmotionMifrationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmotionMifrationsApplication.class, args);
    }
}
