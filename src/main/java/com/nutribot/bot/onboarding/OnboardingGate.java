package com.nutribot.bot.onboarding;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Проверка: прошёл ли пользователь онбординг.
 * Если нет — показываем сообщение и даём кнопку для входа/продолжения.
 */
@Component
@RequiredArgsConstructor
public class OnboardingGate {

    private final UserService userService;
    private final OnboardingService onboardingService;
    private final TelegramClient tg;

    /**
     * Гарантируем, что пользователь существует в БД.
     */
    public Long ensureUser(UpdateContext ctx) {
        return userService.ensureUserByTelegramId(ctx.getTelegramUserId());
    }

    /**
     * true — онбординг пройден;
     * false — нет, пользователю уже показано приглашение.
     */
    public boolean ensureOnboardingCompleted(UpdateContext ctx) {
        Long userId = ensureUser(ctx);
        if (userService.isOnboardingCompleted(userId)) {
            return true;
        }

        sendOnboardingRequired(ctx, userId);
        return false;
    }

    private void sendOnboardingRequired(UpdateContext ctx, Long userId) {
        boolean hasState = onboardingService.hasActiveOnboarding(userId);

        String text;
        TelegramClient.InlineButton button;

        if (hasState) {
            text = """
                    Сначала закончи онбординг 🙌
                    
                    Ты уже начал заполнять данные, осталось несколько шагов.
                    Нажми кнопку ниже, чтобы продолжить.
                    """;
            button = new TelegramClient.InlineButton("Продолжить онбординг", "onb:continue");
        } else {
            text = """
                    Сначала пройди онбординг 🙌
                    
                    Это нужно, чтобы я мог точно считать тренировки и нутриенты под тебя.
                    Нажми кнопку ниже, чтобы начать.
                    """;
            button = new TelegramClient.InlineButton("Пройти онбординг", "onb:start");
        }

        var keyboard = tg.inlineKeyboard(List.of(List.of(button)));
        tg.sendMessage(ctx.getChatId(), text, keyboard);
    }
}
