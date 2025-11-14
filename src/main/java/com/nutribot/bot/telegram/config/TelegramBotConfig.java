package com.nutribot.bot.telegram.config;

import com.pengrad.telegrambot.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация TelegramBot.
 * Здесь только создание бина TelegramBot, без регистрации listener'а.
 */
@Configuration
@EnableConfigurationProperties(TelegramBotProperties.class)
@RequiredArgsConstructor
@Slf4j
public class TelegramBotConfig {

    private final TelegramBotProperties properties;

    @Bean
    public TelegramBot telegramBot() {
        log.info("Initializing Telegram bot");
        return new TelegramBot(properties.token());
    }
}
