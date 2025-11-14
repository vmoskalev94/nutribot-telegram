package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.user.UserLifestyleService;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LifestyleInlineHandler implements BotUpdateHandler {

    private final UserService userService;
    private final UserLifestyleService lifestyleService;
    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();
        return ctx.getCallbackQuery() != null
                && text != null
                && text.startsWith("lifestyle:");
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
            tg.sendMessage(chatId, "Не удалось распознать настройку профиля.");
            return;
        }

        String field = parts[1];
        String value = parts[2];

        switch (field) {
            case "smoke" -> handleSmoking(chatId, userId, value);
            case "vegan" -> handleVegan(chatId, userId, value);
            case "pregnant" -> handlePregnant(chatId, userId, value);
            default -> tg.sendMessage(chatId, "Неизвестный параметр расширенного профиля.");
        }
    }

    private void handleSmoking(Long chatId, Long userId, String value) {
        Double packs;
        String label;
        switch (value) {
            case "0" -> {
                packs = 0.0;
                label = "не курю";
            }
            case "0.2" -> {
                packs = 0.2;
                label = "редко";
            }
            case "1" -> {
                packs = 1.0;
                label = "до 1 пачки в день";
            }
            case "1.5" -> {
                packs = 1.5;
                label = "больше пачки в день";
            }
            default -> {
                tg.sendMessage(chatId, "Не удалось распознать вариант курения.");
                return;
            }
        }

        lifestyleService.updateSmoking(userId, packs);
        tg.sendMessage(chatId, "Сохранил: курение — " + label);
    }

    private void handleVegan(Long chatId, Long userId, String value) {
        boolean vegan;
        String label;
        if ("yes".equals(value)) {
            vegan = true;
            label = "да (веган/вегетарианец)";
        } else if ("no".equals(value)) {
            vegan = false;
            label = "нет";
        } else {
            tg.sendMessage(chatId, "Не удалось распознать значение веганства.");
            return;
        }

        lifestyleService.updateVegan(userId, vegan);
        tg.sendMessage(chatId, "Сохранил: веганство — " + label);
    }

    private void handlePregnant(Long chatId, Long userId, String value) {
        boolean pregnant;
        String label;
        if ("yes".equals(value)) {
            pregnant = true;
            label = "да";
        } else if ("no".equals(value)) {
            pregnant = false;
            label = "нет";
        } else {
            tg.sendMessage(chatId, "Не удалось распознать значение беременности.");
            return;
        }

        lifestyleService.updatePregnant(userId, pregnant);
        tg.sendMessage(chatId, "Сохранил: беременность — " + label);
    }

    @Override
    public int getOrder() {
        return 610; // после нутриент-хендлеров, до fallback
    }
}
