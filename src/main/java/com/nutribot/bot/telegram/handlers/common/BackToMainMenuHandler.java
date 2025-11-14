package com.nutribot.bot.telegram.handlers.common;

import com.nutribot.bot.onboarding.OnboardingGate;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackToMainMenuHandler implements BotUpdateHandler {

    private final OnboardingGate onboardingGate;
    private final TelegramClient tg;

    private static final String MAIN_MENU_TEXT = "Главное меню";

    @Override
    public boolean canHandle(UpdateContext ctx) {
        return ctx.getCallbackQuery() == null
                && MAIN_MENU_TEXT.equals(ctx.getText());
    }

    @Override
    public void handle(UpdateContext ctx) {
        if (!onboardingGate.ensureOnboardingCompleted(ctx)) {
            return;
        }
        Long chatId = ctx.getChatId();
        if (chatId == null) {
            return;
        }
        tg.sendMainMenu(chatId);
    }

    @Override
    public int getOrder() {
        // Обрабатываем до профиля/тренировок/нутриентов
        return 50;
    }
}
