package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.onboarding.OnboardingGate;
import com.nutribot.bot.profile.ProfileEditField;
import com.nutribot.bot.profile.ProfileEditStateStore;
import com.nutribot.bot.profile.ProfileService;
import com.nutribot.bot.profile.UserProfileView;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.telegram.menu.MainMenuButtons;
import com.nutribot.bot.telegram.menu.ProfileMenuButtons;
import com.nutribot.bot.user.UserService;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Раздел «Профиль»:
 * - вход из главного меню;
 * - просмотр профиля;
 * - выбор поля для редактирования.
 */
@Component
@RequiredArgsConstructor
public class ProfileMenuHandler implements BotUpdateHandler {

    private final OnboardingGate onboardingGate;
    private final UserService userService;
    private final ProfileService profileService;
    private final TelegramClient tg;
    private final ProfileEditStateStore editStateStore;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();
        if (text == null) {
            return false;
        }

        return text.equals(MainMenuButtons.PROFILE)
                || text.equals(ProfileMenuButtons.VIEW)
                || text.equals(ProfileMenuButtons.EDIT)
                || text.equals(ProfileMenuButtons.BACK_TO_MAIN)
                || text.equals(ProfileMenuButtons.EDIT_NAME)
                || text.equals(ProfileMenuButtons.EDIT_AGE)
                || text.equals(ProfileMenuButtons.EDIT_HEIGHT)
                || text.equals(ProfileMenuButtons.EDIT_WEIGHT)
                || text.equals(ProfileMenuButtons.EDIT_CITY)
                || text.equals(ProfileMenuButtons.EDIT_PHONE)
                || text.equals(ProfileMenuButtons.EDIT_TRAINING_LEVEL)
                || text.equals(ProfileMenuButtons.EDIT_BACK);
    }

    @Override
    public void handle(UpdateContext ctx) {
        // сначала проверяем онбординг
        if (!onboardingGate.ensureOnboardingCompleted(ctx)) {
            return;
        }

        Long userId = userService.ensureUserByTelegramId(ctx.getTelegramUserId());
        Long chatId = ctx.getChatId();
        String text = ctx.getText();

        if (MainMenuButtons.PROFILE.equals(text)) {
            editStateStore.clear(userId);
            tg.sendMessage(chatId,
                    "Раздел «Профиль». Что дальше?",
                    buildProfileMenuKeyboard());
            return;
        }

        if (ProfileMenuButtons.VIEW.equals(text)) {
            editStateStore.clear(userId);
            UserProfileView view = profileService.getProfile(userId);
            tg.sendMessage(chatId, view.toPrettyString(), buildProfileMenuKeyboard());
            return;
        }

        if (ProfileMenuButtons.EDIT.equals(text)) {
            // вход в режим редактирования — показываем список полей
            editStateStore.clear(userId);
            tg.sendMessage(chatId,
                    """
                            Выберите поле для редактирования:
                            """,
                    buildProfileEditKeyboard());
            return;
        }

        if (ProfileMenuButtons.BACK_TO_MAIN.equals(text)) {
            editStateStore.clear(userId);
            tg.sendMainMenu(chatId);
            return;
        }

        // ===== меню "Редактировать" =====

        if (ProfileMenuButtons.EDIT_BACK.equals(text)) {
            // вернуться в меню профиля
            editStateStore.clear(userId);
            tg.sendMessage(chatId,
                    "Раздел «Профиль». Что дальше?",
                    buildProfileMenuKeyboard());
            return;
        }

        if (ProfileMenuButtons.EDIT_NAME.equals(text)) {
            editStateStore.setField(userId, ProfileEditField.NAME);
            UserProfileView view = profileService.getProfile(userId);

            tg.sendMessage(chatId, """
                    Текущее значение: %s
                    
                    Введите новое значение для поля «Имя»:
                    """.formatted(view.getName()));
            return;
        }

        if (ProfileMenuButtons.EDIT_AGE.equals(text)) {
            editStateStore.setField(userId, ProfileEditField.AGE);
            UserProfileView view = profileService.getProfile(userId);

            tg.sendMessage(chatId, """
                    Текущее значение: %s
                    
                    Введите новое значение для поля «Возраст» (целое число от 10 до 100):
                    """.formatted(view.getAgeLabel()));
            return;
        }


        if (ProfileMenuButtons.EDIT_HEIGHT.equals(text)) {
            editStateStore.setField(userId, ProfileEditField.HEIGHT);
            UserProfileView view = profileService.getProfile(userId);

            tg.sendMessage(chatId, """
                    Текущее значение: %s
                    
                    Введите новый рост в сантиметрах (от 120 до 230):
                    """.formatted(view.getHeightWeightLabel()));
            return;
        }


        if (ProfileMenuButtons.EDIT_WEIGHT.equals(text)) {
            editStateStore.setField(userId, ProfileEditField.WEIGHT);
            UserProfileView view = profileService.getProfile(userId);

            tg.sendMessage(chatId, """
                    Текущее значение: %s
                    
                    Введите новый вес в килограммах (от 35 до 250, можно с точкой или запятой):
                    """.formatted(view.getHeightWeightLabel()));
            return;
        }


        if (ProfileMenuButtons.EDIT_CITY.equals(text)) {
            editStateStore.setField(userId, ProfileEditField.CITY);
            UserProfileView view = profileService.getProfile(userId);

            tg.sendMessage(chatId, """
                    Текущее значение: %s
                    
                    Введите новое значение для поля «Город»:
                    """.formatted(view.getCityLabel()));
            return;
        }


        if (ProfileMenuButtons.EDIT_PHONE.equals(text)) {
            editStateStore.setField(userId, ProfileEditField.PHONE);
            UserProfileView view = profileService.getProfile(userId);

            tg.sendMessage(chatId, """
                    Текущее значение: %s
                    
                    Введите новый номер телефона (можно с пробелами и дефисами):
                    """.formatted(view.getPhoneLabel()));
            return;
        }


        if (ProfileMenuButtons.EDIT_TRAINING_LEVEL.equals(text)) {
            editStateStore.setField(userId, ProfileEditField.TRAINING_LEVEL);
            UserProfileView view = profileService.getProfile(userId);

            String msg = """
                    Текущее значение:
                    %s
                    
                    Выбери новый уровень подготовки:
                    """.formatted(view.getTrainingLevelLabel());

            var beginner = new TelegramClient.InlineButton("Beginner", "profile:level_beginner");
            var amateur = new TelegramClient.InlineButton("Amateur", "profile:level_amateur");
            var pro = new TelegramClient.InlineButton("Pro", "profile:level_pro");

            var keyboard = tg.inlineKeyboard(
                    java.util.List.of(
                            java.util.List.of(beginner),
                            java.util.List.of(amateur),
                            java.util.List.of(pro)
                    )
            );

            tg.sendMessage(chatId, msg, keyboard);
//            return;
        }

    }

    public ReplyKeyboardMarkup buildProfileMenuKeyboard() {
        KeyboardButton view = new KeyboardButton(ProfileMenuButtons.VIEW);
        KeyboardButton edit = new KeyboardButton(ProfileMenuButtons.EDIT);
        KeyboardButton back = new KeyboardButton(ProfileMenuButtons.BACK_TO_MAIN);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[]{view, edit},
                new KeyboardButton[]{back}
        );
        keyboard.resizeKeyboard(true);
        keyboard.oneTimeKeyboard(false);
        keyboard.selective(true);

        return keyboard;
    }

    public ReplyKeyboardMarkup buildProfileEditKeyboard() {
        KeyboardButton name = new KeyboardButton(ProfileMenuButtons.EDIT_NAME);
        KeyboardButton age = new KeyboardButton(ProfileMenuButtons.EDIT_AGE);
        KeyboardButton height = new KeyboardButton(ProfileMenuButtons.EDIT_HEIGHT);
        KeyboardButton weight = new KeyboardButton(ProfileMenuButtons.EDIT_WEIGHT);
        KeyboardButton city = new KeyboardButton(ProfileMenuButtons.EDIT_CITY);
        KeyboardButton phone = new KeyboardButton(ProfileMenuButtons.EDIT_PHONE);
        KeyboardButton level = new KeyboardButton(ProfileMenuButtons.EDIT_TRAINING_LEVEL);
        KeyboardButton back = new KeyboardButton(ProfileMenuButtons.EDIT_BACK);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[]{name, age},
                new KeyboardButton[]{height, weight},
                new KeyboardButton[]{city, phone},
                new KeyboardButton[]{level},
                new KeyboardButton[]{back}
        );
        keyboard.resizeKeyboard(true);
        keyboard.oneTimeKeyboard(false);
        keyboard.selective(true);

        return keyboard;
    }

    @Override
    public int getOrder() {
        // после онбординга, до тренировок/нутриентов
        return 30;
    }
}
