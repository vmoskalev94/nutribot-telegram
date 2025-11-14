package com.nutribot.bot.telegram.client;

import com.nutribot.bot.telegram.menu.MainMenuButtons;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Утилитный клиент для отправки сообщений и построения клавиатур.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramClient {

    private final TelegramBot telegramBot;

    // ===== БАЗОВАЯ ОТПРАВКА СООБЩЕНИЙ =====

    public void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    public void sendMessage(Long chatId, String text, Keyboard keyboard) {
        if (chatId == null || text == null || text.isBlank()) {
            return;
        }
        SendMessage request = new SendMessage(chatId, text);
        if (keyboard != null) {
            request.replyMarkup(keyboard);
        }

        SendResponse response = telegramBot.execute(request);
        if (!response.isOk()) {
            log.warn("Failed to send message: errorCode={}, description={}",
                    response.errorCode(), response.description());
        }
    }

    // ===== ГЛАВНОЕ МЕНЮ =====

    /**
     * Короткое приветствие-заглушка + главное меню.
     * Дальше текст можно будет заменить на финальный.
     */
    public void sendMainMenu(Long chatId) {
        String greeting = """
                Главное меню 🏠
                
                Можно:
                • пройти онбординг или обновить профиль;
                • добавить / посмотреть тренировки;
                • посчитать нутриенты;
                • открыть помощь.
                """;

        sendMessage(chatId, greeting, buildMainMenuKeyboard());
    }

    public ReplyKeyboardMarkup buildMainMenuKeyboard() {
        // Ряды:
        // [Онбординг] [Профиль]
        // [Тренировки] [Нутриенты]
        // [Помощь]
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new String[]{MainMenuButtons.ONBOARDING, MainMenuButtons.PROFILE},
                new String[]{MainMenuButtons.WORKOUTS, MainMenuButtons.NUTRIENTS},
                new String[]{MainMenuButtons.HELP}
        );
        keyboard.resizeKeyboard(true);
        keyboard.oneTimeKeyboard(false);
        keyboard.selective(true);
        return keyboard;
    }

    // ===== БИЛДЕРЫ КЛАВИАТУР =====

    /**
     * Универсальный билдер reply-клавиатуры по спискам строк.
     */
    public ReplyKeyboardMarkup replyKeyboard(List<List<String>> rows) {
        String[][] buttons = rows.stream()
                .map(row -> row.toArray(String[]::new))
                .toArray(String[][]::new);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(buttons);
        keyboard.resizeKeyboard(true);
        keyboard.oneTimeKeyboard(false);
        keyboard.selective(true);
        return keyboard;
    }

    /**
     * Спецификация инлайн-кнопки: текст + callback-data.
     */
    public record InlineButton(String text, String callbackData) {
    }

    /**
     * Универсальный билдер inline-клавиатуры.
     */
    public InlineKeyboardMarkup inlineKeyboard(List<List<InlineButton>> rows) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        for (List<InlineButton> row : rows) {
            InlineKeyboardButton[] btnRow = row.stream()
                    .map(b -> new InlineKeyboardButton(b.text()).callbackData(b.callbackData()))
                    .toArray(InlineKeyboardButton[]::new);
            markup.addRow(btnRow);
        }
        return markup;
    }
}
