package com.nutribot.bot.telegram.dispatcher;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Конвертирует raw Update в UpdateContext и
 * передаёт его подходящему BotUpdateHandler.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateDispatcher {

    private final List<BotUpdateHandler> handlers;

    public void dispatch(Update update) {
        UpdateContext ctx = toContext(update);
        if (ctx == null) {
            log.debug("Skip update without chatId: {}", update);
            return;
        }

        handlers.stream()
                .sorted(Comparator.comparingInt(BotUpdateHandler::getOrder))
                .filter(h -> safeCanHandle(h, ctx))
                .findFirst()
                .ifPresentOrElse(
                        h -> safeHandle(h, ctx),
                        () -> log.debug("No handler found for update: {}", ctx)
                );
    }

    private boolean safeCanHandle(BotUpdateHandler handler, UpdateContext ctx) {
        try {
            return handler.canHandle(ctx);
        } catch (Exception e) {
            log.error("Error in canHandle of {}", handler.getClass().getSimpleName(), e);
            return false;
        }
    }

    private void safeHandle(BotUpdateHandler handler, UpdateContext ctx) {
        try {
            handler.handle(ctx);
        } catch (Exception e) {
            log.error("Error in handle of {}", handler.getClass().getSimpleName(), e);
        }
    }

    private UpdateContext toContext(Update update) {
        Message message = update.message();
        CallbackQuery callback = update.callbackQuery();

        Long chatId = null;
        Long userId = null;
        String text = null;

        if (message != null) {
            if (message.chat() != null) {
                chatId = message.chat().id();
            }
            if (message.from() != null && message.from().id() != null) {
                userId = message.from().id().longValue();
            }
            text = message.text();
        } else if (callback != null) {
            if (callback.message() != null && callback.message().chat() != null) {
                chatId = callback.message().chat().id();
            }
            if (callback.from() != null && callback.from().id() != null) {
                userId = callback.from().id().longValue();
            }
            text = callback.data();
        }

        if (text != null) {
            text = text.trim();
        }

        if (chatId == null || userId == null) {
            return null;
        }

        return UpdateContext.builder()
                .chatId(chatId)
                .telegramUserId(userId)
                .text(text)
                .message(message)
                .callbackQuery(callback)
                .rawUpdate(update)
                .build();
    }
}
