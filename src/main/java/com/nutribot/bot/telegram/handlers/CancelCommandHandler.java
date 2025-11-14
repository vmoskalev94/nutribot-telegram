package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.onboarding.OnboardingService;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * /cancel — сбрасывает текущий онбординг.
 */
@Component
@RequiredArgsConstructor
public class CancelCommandHandler implements BotUpdateHandler {

    private final UserService userService;
    private final OnboardingService onboardingService;
    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();
        return "/cancel".equals(text);
    }

    @Override
    public void handle(UpdateContext ctx) {
        Long userId = userService.ensureUserByTelegramId(ctx.getTelegramUserId());
        boolean hadState = onboardingService.hasActiveOnboarding(userId);

        onboardingService.reset(userId);

        if (hadState && !userService.isOnboardingCompleted(userId)) {
            tg.sendMessage(
                    ctx.getChatId(),
                    """
                    Я отменил текущий онбординг ✋

                    Ты можешь начать его заново командой /start
                    или через кнопку «Онбординг» в главном меню.
                    """,
                    tg.buildMainMenuKeyboard()
            );
        } else {
            tg.sendMessage(
                    ctx.getChatId(),
                    """
                    Сейчас нет активного онбординга.

                    Используй /start, чтобы открыть главное меню
                    или начать онбординг, если он ещё не пройден.
                    """,
                    tg.buildMainMenuKeyboard()
            );
        }
    }

    @Override
    public int getOrder() {
        // до /start и OnboardingFlowHandler
        return 4;
    }
}
