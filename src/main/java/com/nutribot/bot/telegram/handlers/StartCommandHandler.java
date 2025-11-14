package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.onboarding.OnboardingGate;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Хендлер команды /start.
 * Создаёт пользователя (если его ещё нет) и показывает главное меню.
 */
@Component
@RequiredArgsConstructor
public class StartCommandHandler implements BotUpdateHandler {

    private final TelegramClient tg;
    private final OnboardingGate onboardingGate;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();
        return text != null && text.equals("/start");
    }

    @Override
    public void handle(UpdateContext ctx) {
        // гарантируем, что пользователь существует в БД
        onboardingGate.ensureUser(ctx);

        tg.sendMainMenu(ctx.getChatId());
    }

    @Override
    public int getOrder() {
        // Обрабатываем до /ping и других хендлеров
        return 5;
    }
}
