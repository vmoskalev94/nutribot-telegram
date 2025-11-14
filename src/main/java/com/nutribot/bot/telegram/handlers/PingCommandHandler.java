package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Тестовый хендлер для проверки, что бот отвечает.
 */
@Component
@RequiredArgsConstructor
public class PingCommandHandler implements BotUpdateHandler {

    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        return ctx.getText() != null && ctx.getText().equals("/ping");
    }

    @Override
    public void handle(UpdateContext ctx) {
        tg.sendMessage(ctx.getChatId(), "pong 🏓");
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
