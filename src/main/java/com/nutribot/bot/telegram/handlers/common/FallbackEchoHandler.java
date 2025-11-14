package com.nutribot.bot.telegram.handlers.common;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FallbackEchoHandler implements BotUpdateHandler {

    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        // последняя линия обороны — должен стоять с максимальным order
        return true;
    }

    @Override
    public void handle(UpdateContext ctx) {
        Long chatId = ctx.getChatId();
        log.debug("Fallback handler triggered, text='{}'", ctx.getText());

        if (chatId == null) {
            return;
        }

        tg.sendMessage(chatId, """
                Я пока не знаю, как на это ответить 🤔
                Пожалуйста, воспользуйся кнопками главного меню.
                """, tg.buildMainMenuKeyboard());
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
