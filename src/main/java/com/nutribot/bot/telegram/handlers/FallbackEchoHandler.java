package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Фолбэк: пока нет нормальных флоу, отвечает на любой текст.
 * Потом либо удалим, либо поднимем order, чтобы не мешал.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FallbackEchoHandler implements BotUpdateHandler {

    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        return ctx.getText() != null;
    }

    @Override
    public void handle(UpdateContext ctx) {
        log.debug("Fallback handler for text='{}' from user={}", ctx.getText(), ctx.getTelegramUserId());
        tg.sendMessage(ctx.getChatId(),
                "Привет! Я NutriBot. Основные флоу (онбординг, тренировки, нутриенты) появятся чуть позже 🔧");
    }

    @Override
    public int getOrder() {
        return 1000;
    }
}
