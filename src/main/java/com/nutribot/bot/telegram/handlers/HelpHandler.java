package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.telegram.menu.MainMenuButtons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpHandler implements BotUpdateHandler {

    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        if (ctx.getCallbackQuery() != null) {
            return false;
        }
        String text = ctx.getText();
        if (text == null) {
            return false;
        }

        return "/help".equals(text) || MainMenuButtons.HELP.equals(text);  // кнопка «Помощь» в главном меню
    }

    @Override
    public void handle(UpdateContext ctx) {
        Long chatId = ctx.getChatId();
        if (chatId == null) {
            return;
        }

        String helpText = """
                                Я Нутрибoт — бот для спортсменов и просто активных людей 💪
                
                                Доступные команды:
                /start — перезапустить диалог и показать главное меню
                /help — показать эту подсказку
                
                                Что я умею:
                • Онбординг — собираю данные о тебе (возраст, рост, вес, город и т.п.), чтобы точнее считать нагрузки и нутриенты.
                • Мой профиль — покажу, что уже известно, и дам отредактировать отдельные поля.
                • Тренировки — добавлять силовые и кардио, смотреть историю, удалять ненужные.
                • Нутриенты — рассчитать рекомендации по нутриентам на основе профиля и конкретной тренировки.
                
                                Как вводить тренировки:
                
                Силовая:
                • Выбираешь «Тренировки → Добавить → Силовая».
                • После выбора упражнения отправляешь подходы в формате:
                  вес,повторы[,RIR]
                  например: 80,10,2
                • Разделители можно ставить через запятую, пробел, точку с запятой или дефис.
                
                Кардио:
                • «Тренировки → Добавить → Кардио».
                • Указываешь вид (бег, велосипед, ходьба и т.п.).
                • Вводишь длительность (в минутах) и дистанцию (в км, дробную часть можно через запятую или точку).
                • Выбираешь интенсивность (RPE) по кнопкам.
                
                                Важно:
                • Все расчёты носят рекомендательный характер и не заменяют консультацию врача.
                • Если что-то пошло не так — всегда можно вернуться в главное меню по кнопкам снизу.
                """;

        // Клавиатуру не трогаем: используем текущую (там уже главное меню)
        tg.sendMessage(chatId, helpText);
    }

    @Override
    public int getOrder() {
        // Можно поставить рядом с стартовым хендлером
        return 15;
    }
}
