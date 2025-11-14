package com.nutribot.bot.telegram.config;

import com.nutribot.bot.telegram.dispatcher.UpdateDispatcher;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Отдельный компонент, который регистрирует UpdatesListener на TelegramBot.
 * Так мы избегаем циклической зависимости между TelegramBot и UpdateDispatcher.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramUpdatesListenerRegistrar {

    private final TelegramBot telegramBot;
    private final UpdateDispatcher updateDispatcher;

    @PostConstruct
    public void registerUpdatesListener() {
        telegramBot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                try {
                    updateDispatcher.dispatch(update);
                } catch (Exception e) {
                    log.error("Error while handling update {}", update.updateId(), e);
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        log.info("Telegram updates listener registered");
    }
}
