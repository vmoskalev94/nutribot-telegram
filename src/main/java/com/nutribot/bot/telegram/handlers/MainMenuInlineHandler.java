package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Инлайн-кнопка "Главное меню" (callback_data = menu:main).
 */
@Component
@RequiredArgsConstructor
public class MainMenuInlineHandler implements BotUpdateHandler {

    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        return ctx.getCallbackQuery() != null
                && "menu:main".equals(ctx.getText());
    }

    @Override
    public void handle(UpdateContext ctx) {
        tg.sendMainMenu(ctx.getChatId());
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
