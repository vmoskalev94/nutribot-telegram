package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NutrientVerboseInlineHandler implements BotUpdateHandler {

    private final UserService userService;
    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();
        return ctx.getCallbackQuery() != null
                && text != null
                && text.startsWith("nutr:verbose:");
    }

    @Override
    public void handle(UpdateContext ctx) {
        Long chatId = ctx.getChatId();
        Long telegramUserId = ctx.getTelegramUserId();
        if (chatId == null || telegramUserId == null) {
            return;
        }

        Long userId = userService.ensureUserByTelegramId(telegramUserId);
        String data = ctx.getText();
        String[] parts = data.split(":");
        if (parts.length < 3) {
            tg.sendMessage(chatId, "Не удалось обновить настройку подробного режима.");
            return;
        }

        String mode = parts[2];
        boolean verbose;
        if ("on".equals(mode)) {
            verbose = true;
        } else if ("off".equals(mode)) {
            verbose = false;
        } else {
            tg.sendMessage(chatId, "Не удалось распознать значение подробного режима.");
            return;
        }

        userService.updateNutrientVerbose(userId, verbose);

        tg.sendMessage(chatId, verbose
                ? "Подробный режим расчёта нутриентов включён ✅"
                : "Подробный режим расчёта нутриентов выключен.");
    }

    @Override
    public int getOrder() {
        return 605; // рядом с нутриент-хендлерами
    }
}
