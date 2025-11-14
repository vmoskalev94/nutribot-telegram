package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.onboarding.OnboardingFlowHandler;
import com.nutribot.bot.onboarding.OnboardingService;
import com.nutribot.bot.onboarding.OnboardingStep;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Хендлер команды /start.
 * Если онбординг не пройден — запускает его с шага A1.
 * Если уже пройден — показываем главное меню.
 */
@Component
@RequiredArgsConstructor
public class StartCommandHandler implements BotUpdateHandler {

    private final TelegramClient tg;
    private final UserService userService;
    private final OnboardingService onboardingService;
    private final OnboardingFlowHandler onboardingFlowHandler;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();
        return text != null && text.equals("/start");
    }

    @Override
    public void handle(UpdateContext ctx) {
        Long userId = userService.ensureUserByTelegramId(ctx.getTelegramUserId());

        // если онбординг уже завершён — просто главное меню
        if (userService.isOnboardingCompleted(userId)) {
            tg.sendMainMenu(ctx.getChatId());
            return;
        }

        boolean hasState = onboardingService.hasActiveOnboarding(userId);

        if (hasState) {
            // пользователь уже в процессе онбординга — предложим продолжить или начать заново
            OnboardingStep step = onboardingService.getCurrentStep(userId)
                    .orElse(OnboardingStep.A1_GREETING);

            String text = """
                    Ты уже проходишь онбординг 👀
                    
                    Сейчас мы на шаге: %s.
                    Можно продолжить с текущего шага или начать заново.
                    """.formatted(step);

            var cont = new TelegramClient.InlineButton("Продолжить", "onb:continue");
            var restart = new TelegramClient.InlineButton("Начать заново", "onb:restart");
            var keyboard = tg.inlineKeyboard(List.of(List.of(cont, restart)));

            tg.sendMessage(ctx.getChatId(), text, keyboard);
        } else {
            // онбординг ещё не стартовал — начинаем с A1
            onboardingService.start(userId);
            onboardingFlowHandler.handleStep(ctx, OnboardingStep.A1_GREETING);
        }
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
