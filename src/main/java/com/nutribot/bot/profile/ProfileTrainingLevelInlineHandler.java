package com.nutribot.bot.profile;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.telegram.handlers.ProfileMenuHandler;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Обработка инлайн-кнопок выбора уровня подготовки в "Профиль → Редактировать".
 */
@Component
@RequiredArgsConstructor
public class ProfileTrainingLevelInlineHandler implements BotUpdateHandler {

    private final ProfileEditStateStore editStateStore;
    private final UserService userService;
    private final TelegramClient tg;
    private final ProfileMenuHandler profileMenuHandler;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();
        if (ctx.getCallbackQuery() == null || text == null) {
            return false;
        }

        if (!text.startsWith("profile:level_")) {
            return false;
        }

        Long telegramUserId = ctx.getTelegramUserId();
        if (telegramUserId == null) {
            return false;
        }

        Long userId = userService.ensureUserByTelegramId(telegramUserId);
        Optional<ProfileEditField> fieldOpt = editStateStore.getField(userId);

        // реагируем только если сейчас редактируем уровень подготовки
        return fieldOpt.orElse(null) == ProfileEditField.TRAINING_LEVEL;
    }

    @Override
    public void handle(UpdateContext ctx) {
        Long userId = userService.ensureUserByTelegramId(ctx.getTelegramUserId());
        Long chatId = ctx.getChatId();
        String data = ctx.getText(); // profile:level_...

        String level;
        switch (data) {
            case "profile:level_beginner" -> level = "BEGINNER";
            case "profile:level_amateur" -> level = "AMATEUR";
            case "profile:level_pro" -> level = "PRO";
            default -> {
                tg.sendMessage(chatId,
                        "Не получилось распознать уровень подготовки. Попробуй ещё раз.");
                return;
            }
        }

        userService.updateTrainingLevel(userId, level);
        editStateStore.clear(userId);

        tg.sendMessage(chatId,
                "Уровень подготовки обновлён ✅",
                profileMenuHandler.buildProfileEditKeyboard());
    }

    @Override
    public int getOrder() {
        // рядом с ProfileEditHandler, но до fallback-эхо
//      todo return 30; - если не будет ломать онбординг
        return 31;
    }
}
