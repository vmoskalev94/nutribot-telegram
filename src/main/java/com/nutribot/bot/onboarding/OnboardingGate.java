package com.nutribot.bot.onboarding;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Проверка: прошёл ли пользователь онбординг.
 * Если нет — показываем сообщение и даём кнопку для перехода в онбординг.
 */
@Component
@RequiredArgsConstructor
public class OnboardingGate {

    private final UserService userService;
    private final TelegramClient tg;

    /**
     * Обеспечиваем, что пользователь существует в БД.
     *
     * @return userId
     */
    public Long ensureUser(UpdateContext ctx) {
        return userService.ensureUserByTelegramId(ctx.getTelegramUserId());
    }

    /**
     * Проверяем онбординг.
     *
     * @return true — онбординг пройден, можно продолжать;
     * false — онбординг не пройден, пользователю уже отправлено сообщение.
     */
    public boolean ensureOnboardingCompleted(UpdateContext ctx) {
        Long userId = ensureUser(ctx);
        if (userService.isOnboardingCompleted(userId)) {
            return true;
        }

        sendOnboardingRequired(ctx);
        return false;
    }

    private void sendOnboardingRequired(UpdateContext ctx) {
        String text = """
                Сначала пройди онбординг 🙌
                
                Это нужно, чтобы я мог точно считать тренировки и нутриенты под тебя.
                Нажми «Онбординг» в главном меню или кнопку ниже.
                """;

        var button = new TelegramClient.InlineButton("Пройти онбординг", "onb:start");
        var keyboard = tg.inlineKeyboard(List.of(List.of(button)));

        tg.sendMessage(ctx.getChatId(), text, keyboard);
    }
}
