package com.nutribot.bot.profile;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.telegram.handlers.ProfileMenuHandler;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Обработка ввода нового значения для выбранного поля профиля.
 */
@Component
@RequiredArgsConstructor
public class ProfileEditHandler implements BotUpdateHandler {

    private final ProfileEditStateStore editStateStore;
    private final UserService userService;
    private final TelegramClient tg;
    private final ProfileMenuHandler profileMenuHandler;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();
        if (text == null) {
            return false;
        }
        // команды отдаем другим хендлерам
        if (text.startsWith("/")) {
            return false;
        }
        // ВАЖНО: не обрабатываем callbackQuery, только обычные сообщения
        if (ctx.getCallbackQuery() != null) {
            return false;
        }

        Long telegramUserId = ctx.getTelegramUserId();
        if (telegramUserId == null) {
            return false;
        }

        Long userId = userService.ensureUserByTelegramId(telegramUserId);
        Optional<ProfileEditField> fieldOpt = editStateStore.getField(userId);
        return fieldOpt.isPresent();
    }


    @Override
    public void handle(UpdateContext ctx) {
        Long userId = userService.ensureUserByTelegramId(ctx.getTelegramUserId());
        Long chatId = ctx.getChatId();
        String input = ctx.getText().trim();

        ProfileEditField field = editStateStore.getField(userId)
                .orElseThrow(); // canHandle гарантирует, что он есть

        switch (field) {
            case NAME -> handleName(chatId, userId, input);
            case AGE -> handleAge(chatId, userId, input);
            case HEIGHT -> handleHeight(chatId, userId, input);
            case WEIGHT -> handleWeight(chatId, userId, input);
            case CITY -> handleCity(chatId, userId, input);
            case PHONE -> handlePhone(chatId, userId, input);
            case TRAINING_LEVEL -> handleTrainingLevel(chatId, userId, input);
        }
    }

    private void finishEditing(Long chatId, Long userId) {
        editStateStore.clear(userId);
        tg.sendMessage(chatId, "Значение обновлено ✅", profileMenuHandler.buildProfileEditKeyboard());
    }

    // ======== NAME ========

    private void handleName(Long chatId, Long userId, String input) {
        if (!isValidName(input)) {
            tg.sendMessage(chatId,
                    "Кажется, это не очень похоже на имя 🙂\n" +
                            "Введи, пожалуйста, имя от 2 до 50 символов (можно с пробелом/дефисом).");
            return;
        }
        String normalized = normalizeName(input);
        userService.updateName(userId, normalized);
        finishEditing(chatId, userId);
    }

    private boolean isValidName(String raw) {
        if (raw == null) return false;
        String s = raw.trim();
        if (s.length() < 2 || s.length() > 50) return false;
        return s.chars().anyMatch(Character::isLetter);
    }

    private String normalizeName(String raw) {
        String s = raw.trim();
        if (s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ======== AGE ========

    private void handleAge(Long chatId, Long userId, String input) {
        int age;
        try {
            age = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            sendAgeInvalid(chatId);
            return;
        }
        if (age < 10 || age > 100) {
            sendAgeInvalid(chatId);
            return;
        }
        userService.updateAge(userId, age);
        finishEditing(chatId, userId);
    }

    private void sendAgeInvalid(Long chatId) {
        tg.sendMessage(chatId,
                "Нужно указать возраст числом от 10 до 100.\nПопробуй ещё раз 🙂");
    }

    // ======== HEIGHT ========

    private void handleHeight(Long chatId, Long userId, String input) {
        String s = input.replace(",", "."); // на всякий
        int height;
        try {
            height = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            sendHeightInvalid(chatId);
            return;
        }
        if (height < 120 || height > 230) {
            sendHeightInvalid(chatId);
            return;
        }
        userService.updateHeightCm(userId, height);
        finishEditing(chatId, userId);
    }

    private void sendHeightInvalid(Long chatId) {
        tg.sendMessage(chatId,
                "Нужно указать рост числом от 120 до 230 см.\nПопробуй ещё раз 🙂");
    }

    // ======== WEIGHT ========

    private void handleWeight(Long chatId, Long userId, String input) {
        String s = input.replace(",", ".");
        double weight;
        try {
            weight = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            sendWeightInvalid(chatId);
            return;
        }
        if (weight < 35 || weight > 250) {
            sendWeightInvalid(chatId);
            return;
        }
        userService.updateWeightKg(userId, weight);
        finishEditing(chatId, userId);
    }

    private void sendWeightInvalid(Long chatId) {
        tg.sendMessage(chatId,
                "Нужно указать вес числом от 35 до 250 кг.\n" +
                        "Можно использовать точку или запятую как разделитель.");
    }

    // ======== CITY ========

    private void handleCity(Long chatId, Long userId, String input) {
        if (!isValidCity(input)) {
            tg.sendMessage(chatId,
                    "Не получилось распознать город.\n" +
                            "Напиши, пожалуйста, реальное название города, от 2 до 100 символов.");
            return;
        }
        String normalized = normalizeCity(input);
        userService.updateCity(userId, normalized);
        finishEditing(chatId, userId);
    }

    private boolean isValidCity(String raw) {
        String s = raw.trim();
        if (s.length() < 2 || s.length() > 100) return false;
        return s.chars().anyMatch(Character::isLetter);
    }

    private String normalizeCity(String raw) {
        String s = raw.trim();
        if (s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ======== PHONE ========

    private void handlePhone(Long chatId, Long userId, String input) {
        if (!isValidPhone(input)) {
            sendPhoneInvalid(chatId);
            return;
        }
        userService.updatePhone(userId, normalizePhone(input));
        finishEditing(chatId, userId);
    }

    private boolean isValidPhone(String raw) {
        if (raw == null) return false;
        String digits = raw.replaceAll("\\D", "");
        int len = digits.length();
        return len >= 7 && len <= 20;
    }

    private String normalizePhone(String raw) {
        return raw.trim();
    }

    private void sendPhoneInvalid(Long chatId) {
        tg.sendMessage(chatId,
                "Не получилось распознать номер телефона.\n" +
                        "Попробуй ещё раз. Можно писать в формате:\n" +
                        "+79991234567 или 89991234567, допускаются пробелы и дефисы.");
    }

    // ======== TRAINING LEVEL ========

    private void handleTrainingLevel(Long chatId, Long userId, String input) {
        String level = parseTrainingLevel(input);
        if (level == null) {
            tg.sendMessage(chatId,
                    """
                            Не получилось распознать уровень подготовки.
                            
                            Введи один из вариантов:
                            • Beginner / новичок
                            • Amateur / любитель
                            • Pro / профи
                            """);
            return;
        }
        userService.updateTrainingLevel(userId, level);
        finishEditing(chatId, userId);
    }

    private String parseTrainingLevel(String raw) {
        String s = raw.trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "beginner", "новичок", "начинающий" -> "BEGINNER";
            case "amateur", "любитель" -> "AMATEUR";
            case "pro", "профи", "профессионал" -> "PRO";
            default -> null;
        };
    }

    @Override
    public int getOrder() {
        // после ProfileMenuHandler (30), до fallback-эхо
        return 31;
    }
}
