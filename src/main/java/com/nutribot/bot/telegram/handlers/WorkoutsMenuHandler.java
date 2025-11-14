package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.onboarding.OnboardingGate;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.telegram.menu.MainMenuButtons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Вход в раздел «Тренировки» из главного меню.
 * Пока только проверяет онбординг и шлёт заглушку.
 */
@Component
@RequiredArgsConstructor
public class WorkoutsMenuHandler implements BotUpdateHandler {

    private final OnboardingGate onboardingGate;
    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        return MainMenuButtons.WORKOUTS.equals(ctx.getText());
    }

    @Override
    public void handle(UpdateContext ctx) {
        if (!onboardingGate.ensureOnboardingCompleted(ctx)) {
            return;
        }

        tg.sendMessage(ctx.getChatId(), """
                Раздел «Тренировки» скоро будет:
                • добавление силовых и кардио тренировок;
                • список тренировок с пагинацией;
                • карточка тренировки и расчёт нутриентов по ней.
                """);
    }

    @Override
    public int getOrder() {
        return 30;
    }
}
