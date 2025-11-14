package com.nutribot.bot.telegram.dispatcher;

/**
 * Унифицированный интерфейс обработчиков апдейтов.
 */
public interface BotUpdateHandler {

    /**
     * Можно ли обработать данный контекст.
     */
    boolean canHandle(UpdateContext ctx);

    /**
     * Непосредственная обработка.
     */
    void handle(UpdateContext ctx);

    /**
     * Порядок приоритетности обработчика, меньше = раньше.
     */
    default int getOrder() {
        return 100;
    }
}
