package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.onboarding.OnboardingGate;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.telegram.menu.MainMenuButtons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Вход в раздел «Профиль» из главного меню.
 * Пока только проверяет онбординг и шлёт заглушку.
 */
@Component
@RequiredArgsConstructor
public class ProfileMenuHandler implements BotUpdateHandler {

    private final OnboardingGate onboardingGate;
    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        return MainMenuButtons.PROFILE.equals(ctx.getText());
    }

    @Override
    public void handle(UpdateContext ctx) {
        if (!onboardingGate.ensureOnboardingCompleted(ctx)) {
            return;
        }

        tg.sendMessage(ctx.getChatId(), """
                Раздел «Профиль» скоро будет:
                • просмотр базового и расширенного профиля;
                • редактирование отдельных полей.
                
                Здесь мы позже реализуем Этап 2.
                """);
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
