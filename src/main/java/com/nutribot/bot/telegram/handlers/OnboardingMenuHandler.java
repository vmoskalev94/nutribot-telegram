package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.onboarding.OnboardingFlowHandler;
import com.nutribot.bot.onboarding.OnboardingService;
import com.nutribot.bot.onboarding.OnboardingStep;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.telegram.menu.MainMenuButtons;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Вход во флоу онбординга:
 * - кнопка «Онбординг» в главном меню,
 * - inline-кнопки onb:start / onb:continue.
 */
@Component
@RequiredArgsConstructor
public class OnboardingMenuHandler implements BotUpdateHandler {

    private final UserService userService;
    private final OnboardingService onboardingService;
    private final OnboardingFlowHandler onboardingFlowHandler;
    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();
        return MainMenuButtons.ONBOARDING.equals(text)
                || "onb:start".equals(text)
                || "onb:continue".equals(text)
                || "onb:restart".equals(text);
    }

    @Override
    public void handle(UpdateContext ctx) {
        Long userId = userService.ensureUserByTelegramId(ctx.getTelegramUserId());
        String text = ctx.getText();

        if (userService.isOnboardingCompleted(userId)) {
            tg.sendMessage(
                    ctx.getChatId(),
                    """
                            Ты уже прошёл онбординг ✅
                            Если нужно, позже можно будет пройти его заново,
                            а пока доступно всё главное меню.
                            """,
                    tg.buildMainMenuKeyboard()
            );
            return;
        }

        // «Онбординг» / onb:start / onb:restart — всегда начинаем с A1
        if (MainMenuButtons.ONBOARDING.equals(text)
                || "onb:start".equals(text)
                || "onb:restart".equals(text)) {
            onboardingService.start(userId);
        }
        // onb:continue — просто продолжаем с текущего шага

        OnboardingStep step = onboardingService.getCurrentStep(userId)
                .orElse(OnboardingStep.A1_GREETING);

        onboardingFlowHandler.handleStep(ctx, step);
    }

    @Override
    public int getOrder() {
        // сразу после /start, до общего onboading-flow handler
        return 6;
    }
}
