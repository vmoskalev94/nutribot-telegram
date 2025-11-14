package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.onboarding.OnboardingGate;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.user.UserService;
import com.pengrad.telegrambot.model.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LifestyleLocationHandler implements BotUpdateHandler {

    private final OnboardingGate onboardingGate;
    private final UserService userService;
    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        if (ctx.getCallbackQuery() != null) {
            return false;
        }
        if (ctx.getMessage() == null) {
            return false;
        }
        Location loc = ctx.getMessage().location();
        return loc != null;
    }

    @Override
    public void handle(UpdateContext ctx) {
        if (!onboardingGate.ensureOnboardingCompleted(ctx)) {
            return;
        }

        Long chatId = ctx.getChatId();
        Long telegramUserId = ctx.getTelegramUserId();
        if (chatId == null || telegramUserId == null) {
            return;
        }

        Location loc = ctx.getMessage().location();
        Double lat = loc.latitude().doubleValue(); // todo loc.latitude() и  loc.longitude() возвращают Float
        Double lon = loc.longitude().doubleValue();

        Long userId = userService.ensureUserByTelegramId(telegramUserId);
        userService.updateGeo(userId, lat, lon);

        tg.sendMessage(chatId, """
                Геолокация обновлена ✅
                
                В будущем мы будем использовать её, чтобы учитывать:
                • количество солнца;
                • температуру;
                • климат региона при расчёте нутриентов.
                """);
    }

    @Override
    public int getOrder() {
        // После онбординга, до fallback-эхо
        return 220;
    }
}
