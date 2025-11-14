package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.nutrition.NutrientCalculationResult;
import com.nutribot.bot.nutrition.NutrientCalculatorService;
import com.nutribot.bot.nutrition.NutrientExplainProperties;
import com.nutribot.bot.nutrition.NutrientExplanationService;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NutrientExplainHandler implements BotUpdateHandler {

    private final UserService userService;
    private final NutrientExplainProperties props;
    private final NutrientCalculatorService nutrientCalculatorService;
    private final NutrientExplanationService nutrientExplanationService;
    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        if (!props.isUserVisible()) {
            return false; // вообще не реагируем на nutr:explain:*
        }
        String text = ctx.getText();
        return ctx.getCallbackQuery() != null
                && text != null
                && text.startsWith("nutr:explain:");
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
            tg.sendMessage(chatId, "Не удалось распознать тренировку для пояснения.");
            return;
        }

        Long workoutId;
        try {
            workoutId = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            tg.sendMessage(chatId, "Не удалось распознать тренировку для пояснения.");
            return;
        }

        NutrientCalculationResult calc;
        try {
            calc = nutrientCalculatorService.calculateWithContext(userId, workoutId);
        } catch (Exception e) {
            tg.sendMessage(chatId, "Не удалось построить пояснение расчёта для этой тренировки.");
            return;
        }

        String explanation = nutrientExplanationService.buildExplanation(calc);

        // Примитивный чанк по длине (лимит Telegram ~4096)
        int maxLen = 3800;
        int len = explanation.length();
        int offset = 0;
        while (offset < len) {
            int end = Math.min(offset + maxLen, len);
            if (end < len) {
                int nl = explanation.lastIndexOf('\n', end);
                if (nl > offset + 200) { // стараемся резать по строке
                    end = nl;
                }
            }
            String chunk = explanation.substring(offset, end);
            tg.sendMessage(chatId, chunk);
            offset = end;
        }
    }

    @Override
    public int getOrder() {
        return 606;
    }
}
