package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * todo Заглушка на кнопку "Рассчитать нутриенты по этой тренировке".
 * callback_data = workout:nutrients:{workoutId}
 */
@Component
@RequiredArgsConstructor
public class WorkoutNutrientsStubHandler implements BotUpdateHandler {

    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();
        return ctx.getCallbackQuery() != null
                && text != null
                && text.startsWith("workout:nutrients:");
    }

    @Override
    public void handle(UpdateContext ctx) {
        Long chatId = ctx.getChatId();
        // На будущее можем парсить workoutId из callback_data, пока не нужно
        tg.sendMessage(chatId, """
                Здесь будет расчёт нутриентов по этой тренировке 🧪
                
                Реализуем это на этапе «Калькулятор нутриентов».
                А пока можешь зайти в раздел «Нутриенты» из главного меню.
                """);
    }

    @Override
    public int getOrder() {
        return 50;
    }
}
