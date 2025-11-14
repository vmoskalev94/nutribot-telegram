package com.nutribot.bot.onboarding;

import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Общий онбординг-флоу:
 * если у пользователя есть активный state, все апдейты идут сюда.
 */
@Component
@RequiredArgsConstructor
public class OnboardingFlowHandler implements BotUpdateHandler {
    private static final String CITY_MANUAL_BUTTON = "📝 Ввести город вручную";
    private static final String PHONE_MANUAL_BUTTON = "📝 Ввести телефон вручную";
    private static final String SHARE_LOCATION_BUTTON = "📍 Поделиться геопозицией";
    private static final String SHARE_CONTACT_BUTTON = "📱 Поделиться контактом";

    private final OnboardingService onboardingService;
    private final UserService userService;
    private final TelegramClient tg;

    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();
        if (text != null) {
            // эти команды обрабатывают отдельные хендлеры
            if (text.equals("/start") || text.equals("/cancel") || text.equals("/help")) {
                return false;
            }
        }

        Long telegramUserId = ctx.getTelegramUserId();
        if (telegramUserId == null) {
            return false;
        }

        Long userId = userService.ensureUserByTelegramId(telegramUserId);
        return onboardingService.hasActiveOnboarding(userId);
    }


    @Override
    public void handle(UpdateContext ctx) {
        Long userId = userService.ensureUserByTelegramId(ctx.getTelegramUserId());
        OnboardingStep step = onboardingService.getCurrentStep(userId)
                .orElse(OnboardingStep.A1_GREETING);

        handleStepInternal(ctx, userId, step);
    }

    /**
     * Явный вызов из других хендлеров (например, /start или кнопка «Онбординг»).
     */
    public void handleStep(UpdateContext ctx, OnboardingStep step) {
        Long userId = userService.ensureUserByTelegramId(ctx.getTelegramUserId());
        handleStepInternal(ctx, userId, step);
    }

    private void handleStepInternal(UpdateContext ctx, Long userId, OnboardingStep step) {
        switch (step) {
            case A1_GREETING -> handleGreeting(ctx, userId);
            case A2_NAME -> handleName(ctx, userId);
            case A3_SEX -> handleSex(ctx, userId);
            case A4_AGE -> handleAge(ctx, userId);
            case A5_HEIGHT -> handleHeight(ctx, userId);
            case A6_WEIGHT -> handleWeight(ctx, userId);
            case A7_TRAINING_LEVEL -> handleTrainingLevel(ctx, userId);
            case A8_CITY_OR_GEO -> handleCityOrGeo(ctx, userId);
            case A9_PHONE -> handlePhone(ctx, userId);
            case A10_FINISH -> handleFinish(ctx, userId);
            default -> sendGenericStub(ctx, step);
        }
    }

    // ==================== A1 — приветствие ====================

    private void handleGreeting(UpdateContext ctx, Long userId) {
        // Нажали inline-кнопку "Начать"
        if ("onb:a1:start".equals(ctx.getText())) {
            onboardingService.setStep(userId, OnboardingStep.A2_NAME);
            sendNameQuestion(ctx, userId);
            return;
        }

        String text = """
                Привет! Я NutriBot — бот для спортсменов и просто активных людей.
                
                Что я умею:
                • фиксировать силовые и кардио тренировки;
                • считать тренировочный стресс (TSS) и подбирать нутриенты;
                • хранить профиль и давать персональные подсказки.
                
                Чтобы рекомендации были точными, мне нужно немного информации о тебе:
                возраст, пол, антропометрия и уровень подготовки.
                Без базового онбординга дальше не пускаю — сначала настроим всё под тебя 👇
                """;

        var button = new TelegramClient.InlineButton("Начать", "onb:a1:start");
        var keyboard = tg.inlineKeyboard(List.of(List.of(button)));

        tg.sendMessage(ctx.getChatId(), text, keyboard);
    }

    // ==================== A2 — имя ====================

    private void handleName(UpdateContext ctx, Long userId) {
        // Сначала обрабатываем inline-кнопки
        if (ctx.getCallbackQuery() != null) {
            String data = ctx.getText();
            if ("onb:a2:name_tg".equals(data)) {
                handleNameUseTelegram(ctx, userId);
                return;
            }
            if ("onb:a2:name_custom".equals(data)) {
                sendAskCustomName(ctx);
                return;
            }
            // неизвестный callback — просто переспросим
            sendNameQuestion(ctx, userId);
            return;
        }

        // Ожидаем текст — пользователь ввёл своё имя
        String name = ctx.getText();
        if (!isValidName(name)) {
            tg.sendMessage(ctx.getChatId(), """
                    Кажется, это не очень похоже на имя 🙂
                    Введи, пожалуйста, имя от 2 до 50 символов (можно с пробелом/дефисом).
                    """);
            return;
        }

        String normalized = normalizeName(name);
        userService.updateName(userId, normalized);

        onboardingService.setStep(userId, OnboardingStep.A3_SEX);
        sendSexQuestion(ctx);
    }

    private void handleNameUseTelegram(UpdateContext ctx, Long userId) {
        String fromFirstName = ctx.getFirstName();
        String fromUsername = ctx.getUsername();

        String candidate = fromFirstName != null && !fromFirstName.isBlank()
                ? fromFirstName
                : fromUsername;

        if (!isValidName(candidate)) {
            tg.sendMessage(ctx.getChatId(), """
                    Похоже, в Telegram не нашлось подходящего имени.
                    Введи, пожалуйста, имя вручную 🙂
                    """);
            sendAskCustomName(ctx);
            return;
        }

        String normalized = normalizeName(candidate);
        userService.updateName(userId, normalized);

        onboardingService.setStep(userId, OnboardingStep.A3_SEX);
        sendSexQuestion(ctx);
    }

    private void sendNameQuestion(UpdateContext ctx, Long userId) {
        String suggested = ctx.getFirstName();
        if (suggested == null || suggested.isBlank()) {
            suggested = ctx.getUsername();
        }

        String base = "Как к тебе обращаться?\n\n";
        String body;
        if (suggested != null && !suggested.isBlank()) {
            body = "Я вижу в Telegram имя \"%s\". Можем использовать его или ты можешь ввести своё.".formatted(suggested);
        } else {
            body = "Ты можешь ввести любое удобное имя или использовать имя из Telegram (если оно задано).";
        }

        String text = base + body;

        var useTg = new TelegramClient.InlineButton("Использовать Telegram-имя", "onb:a2:name_tg");
        var custom = new TelegramClient.InlineButton("Ввести своё", "onb:a2:name_custom");
        var keyboard = tg.inlineKeyboard(List.of(List.of(useTg), List.of(custom)));

        tg.sendMessage(ctx.getChatId(), text, keyboard);
    }

    private void sendAskCustomName(UpdateContext ctx) {
        String text = """
                Ок! Напиши, пожалуйста, как тебе будет комфортно, например:
                «Макс», «Вика», «Иван П.»
                """;
        tg.sendMessage(ctx.getChatId(), text);
        // step остаётся A2_NAME — следующее текстовое сообщение обработаем как имя
    }

    private boolean isValidName(String raw) {
        if (raw == null) return false;
        String s = raw.trim();
        if (s.length() < 2 || s.length() > 50) return false;
        // хотя бы одна буква
        return s.chars().anyMatch(Character::isLetter);
    }

    private String normalizeName(String raw) {
        String s = raw.trim();
        if (s.isEmpty()) {
            return s;
        }
        // простейшая нормализация: первая буква заглавная, остальное как есть
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ==================== A3 — пол ====================

    private void handleSex(UpdateContext ctx, Long userId) {
        if (ctx.getCallbackQuery() != null) {
            String data = ctx.getText();
            if ("onb:a3:sex_m".equals(data)) {
                userService.updateSex(userId, "M");
                onboardingService.setStep(userId, OnboardingStep.A4_AGE);
                sendAgeQuestion(ctx);
                return;
            }
            if ("onb:a3:sex_f".equals(data)) {
                userService.updateSex(userId, "F");
                onboardingService.setStep(userId, OnboardingStep.A4_AGE);
                sendAgeQuestion(ctx);
                return;
            }
        }

        tg.sendMessage(ctx.getChatId(), "Выбери, пожалуйста, пол с помощью кнопок ниже.");
        sendSexQuestion(ctx);
    }

    private void sendSexQuestion(UpdateContext ctx) {
        String text = """
                Уточним пол — это важно для расчёта веса мышц, костей и ряда нутриентов.
                
                Выбери вариант:
                """;

        var male = new TelegramClient.InlineButton("М", "onb:a3:sex_m");
        var female = new TelegramClient.InlineButton("Ж", "onb:a3:sex_f");
        var keyboard = tg.inlineKeyboard(List.of(List.of(male, female)));

        tg.sendMessage(ctx.getChatId(), text, keyboard);
    }

    // ==================== A4 — возраст ====================

    private void handleAge(UpdateContext ctx, Long userId) {
        String text = ctx.getText();
        if (text == null || text.isBlank()) {
            sendAgeInvalid(ctx);
            return;
        }

        text = text.trim();
        int age;
        try {
            age = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            sendAgeInvalid(ctx);
            return;
        }

        if (age < 10 || age > 100) {
            sendAgeInvalid(ctx);
            return;
        }

        userService.updateAge(userId, age);
        onboardingService.setStep(userId, OnboardingStep.A5_HEIGHT);

        tg.sendMessage(ctx.getChatId(), "Записал возраст ✅");
        sendHeightQuestion(ctx);
    }

    private void sendAgeQuestion(UpdateContext ctx) {
        String text = """
                Сколько тебе лет? 🙂
                
                Напиши целое число от 10 до 100.
                """;
        tg.sendMessage(ctx.getChatId(), text);
    }

    private void sendAgeInvalid(UpdateContext ctx) {
        String text = """
                Нужно указать возраст числом от 10 до 100.
                Попробуй ещё раз 🙂
                """;
        tg.sendMessage(ctx.getChatId(), text);
    }

    // ==================== заглушка для ещё не реализованных шагов ====================

    private void sendGenericStub(UpdateContext ctx, OnboardingStep step) {
        String text = "Онбординг в разработке.\nТекущий шаг: " + step;
        tg.sendMessage(ctx.getChatId(), text);
    }

    @Override
    public int getOrder() {
        // после /start и хендлера кнопки «Онбординг», но до остальных
        return 15;
    }

    // ==================== A5 — рост ====================

    private void handleHeight(UpdateContext ctx, Long userId) {
        String text = ctx.getText();
        if (text == null || text.isBlank()) {
            sendHeightInvalid(ctx);
            return;
        }

        text = text.trim().replace(",", "."); // на всякий, хотя ожидаем целое

        int height;
        try {
            height = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            sendHeightInvalid(ctx);
            return;
        }

        if (height < 120 || height > 230) {
            sendHeightInvalid(ctx);
            return;
        }

        userService.updateHeightCm(userId, height);
        onboardingService.setStep(userId, OnboardingStep.A6_WEIGHT);

        tg.sendMessage(ctx.getChatId(), "Рост записал ✅");
        sendWeightQuestion(ctx);
    }

    private void sendHeightQuestion(UpdateContext ctx) {
        String text = """
                Теперь укажем рост.
                
                Напиши рост в сантиметрах, целым числом, например:
                170
                182
                """;
        tg.sendMessage(ctx.getChatId(), text);
    }

    private void sendHeightInvalid(UpdateContext ctx) {
        String text = """
                Нужно указать рост числом от 120 до 230 сантиметров.
                Попробуй ещё раз 🙂
                """;
        tg.sendMessage(ctx.getChatId(), text);
    }

    // ==================== A6 — вес ====================

    private void handleWeight(UpdateContext ctx, Long userId) {
        String text = ctx.getText();
        if (text == null || text.isBlank()) {
            sendWeightInvalid(ctx);
            return;
        }

        text = text.trim().replace(",", "."); // поддерживаем "70,5" и "70.5"

        double weight;
        try {
            weight = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            sendWeightInvalid(ctx);
            return;
        }

        if (weight < 35 || weight > 250) {
            sendWeightInvalid(ctx);
            return;
        }

        userService.updateWeightKg(userId, weight);
        onboardingService.setStep(userId, OnboardingStep.A7_TRAINING_LEVEL);

        tg.sendMessage(ctx.getChatId(), "Вес записал ✅");
        sendTrainingLevelQuestion(ctx);
    }

    private void sendWeightQuestion(UpdateContext ctx) {
        String text = """
                Укажи вес в килограммах.
                
                Можно с дробной частью, через точку или запятую, например:
                70
                63.5
                81,2
                """;
        tg.sendMessage(ctx.getChatId(), text);
    }

    private void sendWeightInvalid(UpdateContext ctx) {
        String text = """
                Нужно указать вес числом от 35 до 250 кг.
                Можно использовать точку или запятую как разделитель.
                Попробуй ещё раз 🙂
                """;
        tg.sendMessage(ctx.getChatId(), text);
    }

    // ==================== A7 — уровень подготовки ====================

    private void handleTrainingLevel(UpdateContext ctx, Long userId) {
        if (ctx.getCallbackQuery() != null) {
            String data = ctx.getText();
            switch (data) {
                case "onb:a7:level_beginner" -> {
                    userService.updateTrainingLevel(userId, "BEGINNER");
                    onboardingService.setStep(userId, OnboardingStep.A8_CITY_OR_GEO);
                    sendAfterTrainingLevel(ctx);
                    return;
                }
                case "onb:a7:level_amateur" -> {
                    userService.updateTrainingLevel(userId, "AMATEUR");
                    onboardingService.setStep(userId, OnboardingStep.A8_CITY_OR_GEO);
                    sendAfterTrainingLevel(ctx);
                    return;
                }
                case "onb:a7:level_pro" -> {
                    userService.updateTrainingLevel(userId, "PRO");
                    onboardingService.setStep(userId, OnboardingStep.A8_CITY_OR_GEO);
                    sendAfterTrainingLevel(ctx);
                    return;
                }
                default -> {
                    // неизвестный callback — просто заново спросим
                    sendTrainingLevelQuestion(ctx);
                    return;
                }
            }
        }

        // если пришёл обычный текст, не мучаем пользователя парсингом — просим нажать кнопки
        tg.sendMessage(ctx.getChatId(),
                "Выбери, пожалуйста, уровень подготовки с помощью кнопок ниже.");
        sendTrainingLevelQuestion(ctx);
    }

    private void sendTrainingLevelQuestion(UpdateContext ctx) {
        String text = """
                На каком уровне ты сейчас в тренировках?
                
                • Beginner — только начинаю или тренируюсь нерегулярно.
                • Amateur — тренируюсь стабильно 3–4 раза в неделю.
                • Pro — высокий объём/интенсивность, спорт важная часть жизни.
                
                Выбери вариант:
                """;

        var beginner = new TelegramClient.InlineButton("Beginner", "onb:a7:level_beginner");
        var amateur = new TelegramClient.InlineButton("Amateur", "onb:a7:level_amateur");
        var pro = new TelegramClient.InlineButton("Pro", "onb:a7:level_pro");

        var keyboard = tg.inlineKeyboard(List.of(List.of(beginner), List.of(amateur), List.of(pro)));

        tg.sendMessage(ctx.getChatId(), text, keyboard);
    }

    private void sendAfterTrainingLevel(UpdateContext ctx) {
        String text = """
                Уровень подготовки записал ✅
                
                Теперь давай определимся с городом/геолокацией.
                """;
        tg.sendMessage(ctx.getChatId(), text);
        sendCityQuestion(ctx);
    }

    // ==================== A8 — город / геолокация ====================

    private void handleCityOrGeo(UpdateContext ctx, Long userId) {
        Message msg = ctx.getMessage();

        // 1) Пользователь поделился геопозицией
        if (msg != null && msg.location() != null) {
            Location loc = msg.location();
            Double lat = loc.latitude() != null ? loc.latitude().doubleValue() : null;
            Double lon = loc.longitude() != null ? loc.longitude().doubleValue() : null;

            if (lat != null && lon != null) {
                userService.updateGeo(userId, lat, lon);
            }

            onboardingService.setStep(userId, OnboardingStep.A9_PHONE);
            tg.sendMessage(ctx.getChatId(), "Геолокацию записал ✅");
            sendPhoneQuestion(ctx);
            return;
        }

        // 2) Пользователь нажал "Ввести город вручную"
        String text = ctx.getText();
        if (CITY_MANUAL_BUTTON.equals(text)) {
            sendCityManualInstruction(ctx);
            return;
        }

        // 3) На некоторых клиентах вместо location приходит текст кнопки гео
        if (SHARE_LOCATION_BUTTON.equals(text)) {
            tg.sendMessage(ctx.getChatId(), """
                    Похоже, Telegram не смог отправить геопозицию.
                    
                    Можно:
                    • попробовать ещё раз с телефона, дав доступ к геолокации;
                    • или просто написать название города вручную.
                    """);
            return;
        }

        // 4) Пользователь ввёл текст с названием города
        if (text == null || text.isBlank()) {
            sendCityInvalid(ctx);
            return;
        }

        String city = text.trim();
        if (!isValidCity(city)) {
            sendCityInvalid(ctx);
            return;
        }

        String normalized = normalizeCity(city);
        userService.updateCity(userId, normalized);
        onboardingService.setStep(userId, OnboardingStep.A9_PHONE);

        tg.sendMessage(ctx.getChatId(), "Город записал ✅");
        sendPhoneQuestion(ctx);
    }

    private void sendCityQuestion(UpdateContext ctx) {
        String text = """
                Теперь город или геолокация.
                
                Это нужно, чтобы в будущем учитывать климат, длительность дня и другие факторы.
                
                Можно:
                • поделиться текущей геопозицией;
                • или просто написать город вручную.
                """;

        KeyboardButton locBtn = new KeyboardButton(SHARE_LOCATION_BUTTON).requestLocation(true);
        KeyboardButton manualBtn = new KeyboardButton(CITY_MANUAL_BUTTON);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[]{locBtn},
                new KeyboardButton[]{manualBtn}
        );
        keyboard.resizeKeyboard(true);
        keyboard.oneTimeKeyboard(true);
        keyboard.selective(true);

        tg.sendMessage(ctx.getChatId(), text, keyboard);
    }

    private void sendCityManualInstruction(UpdateContext ctx) {
        String text = """
                Ок! Напиши, пожалуйста, название города, например:
                «Москва», «Стокгольм», «Санкт-Петербург».
                """;
        tg.sendMessage(ctx.getChatId(), text);
    }

    private void sendCityInvalid(UpdateContext ctx) {
        String text = """
                Не получилось распознать город.
                
                Напиши, пожалуйста, реальное название города, от 2 до 100 символов.
                """;
        tg.sendMessage(ctx.getChatId(), text);
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

    // ==================== A9 — телефон ====================

    private void handlePhone(UpdateContext ctx, Long userId) {
        Message msg = ctx.getMessage();

        // 1) Пользователь поделился контактом
        if (msg != null && msg.contact() != null) {
            Contact contact = msg.contact();
            String phone = contact.phoneNumber();

            if (!isValidPhone(phone)) {
                sendPhoneInvalid(ctx);
                return;
            }

            userService.updatePhone(userId, normalizePhone(phone));
            onboardingService.setStep(userId, OnboardingStep.A10_FINISH);
            handleFinish(ctx, userId);
            return;
        }

        // 2) Пользователь нажал "Ввести телефон вручную"
        String text = ctx.getText();
        if (PHONE_MANUAL_BUTTON.equals(text)) {
            sendPhoneManualInstruction(ctx);
            return;
        }

        // 3) Некоторые клиенты могут прислать только текст кнопки контакта
        if (SHARE_CONTACT_BUTTON.equals(text)) {
            tg.sendMessage(ctx.getChatId(), """
                    Похоже, Telegram не смог отправить контакт.
                    
                    Можно:
                    • попробовать ещё раз с телефона;
                    • или ввести номер вручную.
                    """);
            return;
        }

        // 4) Пользователь ввёл телефон текстом
        if (text == null || text.isBlank()) {
            sendPhoneInvalid(ctx);
            return;
        }

        if (!isValidPhone(text)) {
            sendPhoneInvalid(ctx);
            return;
        }

        userService.updatePhone(userId, normalizePhone(text));
        onboardingService.setStep(userId, OnboardingStep.A10_FINISH);
        handleFinish(ctx, userId);
    }

    private void sendPhoneQuestion(UpdateContext ctx) {
        String text = """
                Остался телефон.
                
                Я не буду звонить 🙂 Он нужен, чтобы:
                • можно было восстановить доступ к профилю;
                • в будущем — для напоминаний (если ты сам включишь).
                
                Можно:
                • поделиться своим контактом кнопкой;
                • или ввести номер вручную.
                """;

        KeyboardButton contactBtn = new KeyboardButton(SHARE_CONTACT_BUTTON).requestContact(true);
        KeyboardButton manualBtn = new KeyboardButton(PHONE_MANUAL_BUTTON);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[]{contactBtn},
                new KeyboardButton[]{manualBtn}
        );
        keyboard.resizeKeyboard(true);
        keyboard.oneTimeKeyboard(true);
        keyboard.selective(true);

        tg.sendMessage(ctx.getChatId(), text, keyboard);
    }

    private void sendPhoneManualInstruction(UpdateContext ctx) {
        String text = """
                Напиши номер телефона в свободном формате, например:
                +79991234567
                89991234567
                +46 70 123 45 67
                
                Можно использовать пробелы, дефисы и скобки.
                """;
        tg.sendMessage(ctx.getChatId(), text);
    }

    private void sendPhoneInvalid(UpdateContext ctx) {
        String text = """
                Не получилось распознать номер телефона.
                
                Попробуй ещё раз. Можно писать в формате:
                +79991234567 или 89991234567, допускаются пробелы и дефисы.
                """;
        tg.sendMessage(ctx.getChatId(), text);
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

    // ==================== A10 — завершение ====================

    private void handleFinish(UpdateContext ctx, Long userId) {
        // помечаем онбординг как завершённый и очищаем state
        userService.markOnboardingCompleted(userId);
        onboardingService.reset(userId);

        String text = """
                Готово! Базовый онбординг завершён ✅
                
                Теперь тебе доступны:
                • «Профиль» — посмотреть и изменить данные;
                • «Тренировки» — добавлять и просматривать сессии;
                • «Нутриенты» — считать нутриенты по тренировкам.
                
                Можешь пользоваться кнопками главного меню ниже.
                """;

        tg.sendMessage(ctx.getChatId(), text, tg.buildMainMenuKeyboard());
    }

}
