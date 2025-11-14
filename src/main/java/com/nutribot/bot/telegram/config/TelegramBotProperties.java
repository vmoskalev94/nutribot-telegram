package com.nutribot.bot.telegram.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки Telegram-бота.
 * Токен читается из nutribot.telegram.token.
 */
@ConfigurationProperties(prefix = "nutribot.telegram")
public record TelegramBotProperties(@NotBlank String token) {

}
