package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.onboarding.OnboardingGate;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.telegram.menu.MainMenuButtons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Вход в раздел «Нутриенты» из главного меню.
 * Пока только проверяет онбординг и шлёт заглушку.
 */
@Component
@RequiredArgsConstructor
public class NutrientsMenuHandler implements BotUpdateHandler {

    private final OnboardingGate onboardingGate;
    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        return MainMenuButtons.NUTRIENTS.equals(ctx.getText());
    }

    @Override
    public void handle(UpdateContext ctx) {
        if (!onboardingGate.ensureOnboardingCompleted(ctx)) {
            return;
        }

        tg.sendMessage(ctx.getChatId(), """
                Раздел «Нутриенты»:
                • расширенный профиль (курение, веганство, беременность, гео);
                • расчёт нутриентов по последней тренировке;
                • статистика по нутриентам (позже).
                
                Пока это заглушка, реальная логика будет на Этапе 4.
                """);
    }

    @Override
    public int getOrder() {
        return 40;
    }
}
