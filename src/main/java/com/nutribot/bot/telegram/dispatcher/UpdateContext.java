package com.nutribot.bot.telegram.dispatcher;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.Builder;
import lombok.Value;

/**
 * Упрощённый контекст апдейта, чтобы хендлеры не лазили в raw Update.
 */
@Value
@Builder
public class UpdateContext {

    Long chatId;
    Long telegramUserId;
    String text;              // очищенный текст сообщения или data из callback
    Message message;
    CallbackQuery callbackQuery;
    Update rawUpdate;

    public boolean hasCallback() {
        return callbackQuery != null;
    }

    public String getUsername() {
        if (message != null && message.from() != null) {
            return message.from().username();
        }
        if (callbackQuery != null && callbackQuery.from() != null) {
            return callbackQuery.from().username();
        }
        return null;
    }

    public String getFirstName() {
        if (message != null && message.from() != null) {
            return message.from().firstName();
        }
        if (callbackQuery != null && callbackQuery.from() != null) {
            return callbackQuery.from().firstName();
        }
        return null;
    }

    public boolean isCommand(String command) {
        return text != null && text.equals(command);
    }
}
