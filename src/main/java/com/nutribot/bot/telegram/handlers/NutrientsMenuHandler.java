package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.nutrition.*;
import com.nutribot.bot.onboarding.OnboardingGate;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.telegram.menu.MainMenuButtons;
import com.nutribot.bot.telegram.menu.NutrientsMenuButtons;
import com.nutribot.bot.user.User;
import com.nutribot.bot.user.UserLifestyle;
import com.nutribot.bot.user.UserLifestyleService;
import com.nutribot.bot.user.UserService;
import com.nutribot.bot.workout.Workout;
import com.nutribot.bot.workout.WorkoutService;
import com.nutribot.bot.workout.WorkoutType;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NutrientsMenuHandler implements BotUpdateHandler {

    private final OnboardingGate onboardingGate;
    private final UserService userService;
    private final WorkoutService workoutService;
    private final NutrientExplainProperties props;
    private final NutrientCalculatorService nutrientCalculatorService;
    private final UserLifestyleService userLifestyleService;
    private final NutrientStatsService nutrientStatsService;
    private final NutrientExplanationService nutrientExplanationService;
    private final TelegramClient tg;

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public boolean canHandle(UpdateContext ctx) {
        if (ctx.getCallbackQuery() != null) {
            return false;
        }

        String text = ctx.getText();
        if (text == null) {
            return false;
        }

        if (!props.isUserVisible()
                && text.equals(NutrientsMenuButtons.VERBOSE_MODE)) {
            return false;
        }

        return text.equals(MainMenuButtons.NUTRIENTS)
                || text.equals(NutrientsMenuButtons.FILL_PROFILE)
                || text.equals(NutrientsMenuButtons.LAST_WORKOUT)
                || text.equals(NutrientsMenuButtons.STATS)
                || text.equals(NutrientsMenuButtons.VERBOSE_MODE)
                || text.equals(NutrientsMenuButtons.BACK_TO_MAIN)
                || text.equals(NutrientsMenuButtons.SMOKING)
                || text.equals(NutrientsMenuButtons.VEGAN)
                || text.equals(NutrientsMenuButtons.PREGNANCY)
                || text.equals(NutrientsMenuButtons.GEO)
                || text.equals(NutrientsMenuButtons.BACK_TO_NUTRIENTS);
    }

    @Override
    public void handle(UpdateContext ctx) {
        if (!onboardingGate.ensureOnboardingCompleted(ctx)) {
            return;
        }

        Long chatId = ctx.getChatId();
        Long userId = userService.ensureUserByTelegramId(ctx.getTelegramUserId());
        String text = ctx.getText();

        if (MainMenuButtons.NUTRIENTS.equals(text)) {
            sendNutrientsMenuIntro(chatId);
            return;
        }

        if (NutrientsMenuButtons.FILL_PROFILE.equals(text)) {
            sendExtendedProfileMenu(chatId, userId);
            return;
        }

        if (NutrientsMenuButtons.VERBOSE_MODE.equals(text)) {
            sendVerboseModeSettings(chatId, userId);
            return;
        }

        if (NutrientsMenuButtons.SMOKING.equals(text)) {
            sendSmokingOptions(chatId, userId);
            return;
        }

        if (NutrientsMenuButtons.VEGAN.equals(text)) {
            sendVeganOptions(chatId, userId);
            return;
        }

        if (NutrientsMenuButtons.PREGNANCY.equals(text)) {
            sendPregnancyOptions(chatId, userId);
            return;
        }

        if (NutrientsMenuButtons.GEO.equals(text)) {
            sendGeoInstructions(chatId, userId);
            return;
        }

        if (NutrientsMenuButtons.BACK_TO_NUTRIENTS.equals(text)) {
            sendNutrientsMenuIntro(chatId);
            return;
        }

        if (NutrientsMenuButtons.LAST_WORKOUT.equals(text)) {
            handleLastWorkoutNutrients(chatId, userId);
            return;
        }

        if (NutrientsMenuButtons.STATS.equals(text)) {
            NutrientStatsResult stats = nutrientStatsService.getWeeklyStats(userId);
            sendStatsStub(chatId, stats);
            return;
        }

        if (NutrientsMenuButtons.BACK_TO_MAIN.equals(text)) {
            tg.sendMainMenu(chatId);
        }
    }

    private void sendVerboseModeSettings(Long chatId, Long userId) {
        User user = userService.getUserOrThrow(userId);
        boolean verbose = Boolean.TRUE.equals(user.getNutrientVerbose());

        String status = verbose ? "включён ✅" : "выключен";

        String text = """
                Подробный режим расчёта нутриентов 🧾
                
                Сейчас: %s.
                
                В подробном режиме бот после краткого результата
                высылает детальный лог с формулами и подстановкой значений.
                
                Можно включить или выключить режим кнопками ниже.
                """.formatted(status);

        var onBtn = new TelegramClient.InlineButton("Включить подробный режим", "nutr:verbose:on");
        var offBtn = new TelegramClient.InlineButton("Выключить подробный режим", "nutr:verbose:off");

        var kb = tg.inlineKeyboard(List.of(List.of(onBtn), List.of(offBtn)));

        tg.sendMessage(chatId, text, kb);
    }

    private void sendNutrientsMenuIntro(Long chatId) {
        tg.sendMessage(chatId, """
                Раздел «Нутриенты».
                
                Здесь можно:
                • рассчитать, какие витамины и микроэлементы особенно важны после тренировки;
                • позже — заполнить расширенный профиль для более точных рекомендаций;
                • в будущем — смотреть статистику по нутриентам за период.
                
                С чего начнём?
                """, buildNutrientsMenuKeyboard());
    }

    private ReplyKeyboardMarkup buildNutrientsMenuKeyboard() {
        KeyboardButton fill = new KeyboardButton(NutrientsMenuButtons.FILL_PROFILE);
        KeyboardButton last = new KeyboardButton(NutrientsMenuButtons.LAST_WORKOUT);
        KeyboardButton verbose = new KeyboardButton(NutrientsMenuButtons.VERBOSE_MODE);
        KeyboardButton stats = new KeyboardButton(NutrientsMenuButtons.STATS);
        KeyboardButton back = new KeyboardButton(NutrientsMenuButtons.BACK_TO_MAIN);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[]{fill},
                new KeyboardButton[]{last},
                new KeyboardButton[]{stats},
                new KeyboardButton[]{back}
        );
        if (props.isUserVisible()) keyboard.addRow(verbose);

        keyboard.resizeKeyboard(true);
        keyboard.oneTimeKeyboard(false);
        keyboard.selective(true);

        return keyboard;
    }

    private ReplyKeyboardMarkup buildExtendedProfileKeyboard() {
        KeyboardButton smoking = new KeyboardButton(NutrientsMenuButtons.SMOKING);
        KeyboardButton vegan = new KeyboardButton(NutrientsMenuButtons.VEGAN);
        KeyboardButton pregnancy = new KeyboardButton(NutrientsMenuButtons.PREGNANCY);
        KeyboardButton geo = new KeyboardButton(NutrientsMenuButtons.GEO);
        KeyboardButton back = new KeyboardButton(NutrientsMenuButtons.BACK_TO_NUTRIENTS);

        ReplyKeyboardMarkup kb = new ReplyKeyboardMarkup(
                new KeyboardButton[]{smoking},
                new KeyboardButton[]{vegan},
                new KeyboardButton[]{pregnancy},
                new KeyboardButton[]{geo},
                new KeyboardButton[]{back}
        );
        kb.resizeKeyboard(true);
        kb.oneTimeKeyboard(false);
        kb.selective(true);
        return kb;
    }

    private void handleLastWorkoutNutrients(Long chatId, Long userId) {
        var lastOpt = workoutService.findLastActiveWorkout(userId);
        if (lastOpt.isEmpty()) {
            tg.sendMessage(chatId, """
                    У тебя пока нет сохранённых тренировок 🕒
                    
                    Чтобы получить рекомендации по нутриентам:
                    1) зайди в раздел «Тренировки»;
                    2) добавь и сохрани хотя бы одну тренировку;
                    3) вернись сюда и выбери «Расчёт нутриентов последней тренировки».
                    """, buildNutrientsMenuKeyboard());
            return;
        }

        var w = lastOpt.get();

        NutrientCalculationResult calc =
                nutrientCalculatorService.calculateWithContext(userId, w.getId());
        List<NutrientRecommendation> recs = calc.recommendations();

        String typeLabel = (w.getType() == WorkoutType.STRENGTH)
                ? "Силовая тренировка"
                : "Кардио-тренировка";

        String dt = (w.getStartedAt() != null)
                ? w.getStartedAt().toLocalDateTime().format(DATE_TIME_FMT)
                : "дата не указана";

        StringBuilder sb = new StringBuilder();
        sb.append("Расчёт нутриентов по последней тренировке:\n\n");
        sb.append("Дата: ").append(dt).append("\n");
        sb.append("Тип: ").append(typeLabel).append("\n\n");

        if (recs.isEmpty()) {
            sb.append("""
                    Пока нет конкретных рекомендаций — формулы ещё в процессе настройки.
                    
                    На следующих шагах сюда подтянем полноценный список нутриентов.
                    """);
        } else {
            sb.append("Рекомендации:\n");
            for (NutrientRecommendation r : recs) {
                sb.append("• ")
                        .append(r.name())
                        .append(" (").append(r.code()).append("): ")
                        .append(formatAmount(r.value()))
                        .append(" ").append(r.unit())
                        .append("\n");
            }
            sb.append("\nЗначения примерные и не заменяют консультацию с врачом.");
        }

        if (props.isUserVisible()) {
            var explainBtn = new TelegramClient.InlineButton(
                    "Пояснить расчёт",
                    "nutr:explain:" + w.getId()
            );
            var kb = tg.inlineKeyboard(List.of(List.of(explainBtn)));
            tg.sendMessage(chatId, sb.toString(), kb);
        } else {
            tg.sendMessage(chatId, sb.toString());
        }

//        var explainBtn = new TelegramClient.InlineButton(
//                "Пояснить расчёт",
//                "nutr:explain:" + w.getId()
//        );
//        var kb = tg.inlineKeyboard(List.of(List.of(explainBtn)));
//
//        tg.sendMessage(chatId, sb.toString(), kb);

        User user = userService.getUserOrThrow(userId);
        boolean verbose = Boolean.TRUE.equals(user.getNutrientVerbose());
        if (verbose) {
            String explanation = nutrientExplanationService.buildExplanation(calc);
            tg.sendMessage(chatId, explanation);
        }
    }


    private String formatAmount(double value) {
        if (value < 10) {
            return String.format("%.1f", value);
        }
        return String.format("%.0f", value);
    }

    private void sendSmokingOptions(Long chatId, Long userId) {
        UserLifestyle lifestyle = userLifestyleService.findByUserId(userId).orElse(null);
        String current = "не указано";
        if (lifestyle != null && lifestyle.getSmokePacksPerDay() != null) {
            double p = lifestyle.getSmokePacksPerDay();
            if (p == 0.0) current = "не курю";
            else if (p <= 0.2) current = "редко";
            else if (p <= 1.0) current = "до 1 пачки в день";
            else current = "больше пачки в день";
        }

        String text = """
                Настройка: курение 🚬
                
                Текущее значение: %s
                
                Выбери, пожалуйста, подходящий вариант:
                """.formatted(current);

        var none = new TelegramClient.InlineButton("Не курю", "lifestyle:smoke:0");
        var rare = new TelegramClient.InlineButton("Редко", "lifestyle:smoke:0.2");
        var daily = new TelegramClient.InlineButton("До 1 пачки в день", "lifestyle:smoke:1");
        var heavy = new TelegramClient.InlineButton("Больше пачки в день", "lifestyle:smoke:1.5");

        var kb = tg.inlineKeyboard(
                List.of(
                        List.of(none),
                        List.of(rare),
                        List.of(daily),
                        List.of(heavy)
                )
        );

        tg.sendMessage(chatId, text, kb);
    }

    private void sendVeganOptions(Long chatId, Long userId) {
        UserLifestyle lifestyle = userLifestyleService.findByUserId(userId).orElse(null);
        String current = (lifestyle != null && Boolean.TRUE.equals(lifestyle.getVegan()))
                ? "да"
                : "нет";

        String text = """
                Настройка: веганство 🌱
                
                Текущее значение: %s
                
                Мы учитываем это при расчётах нутриентов,
                связанных с дефицитом в растительном рационе.
                """.formatted(current);

        var noBtn = new TelegramClient.InlineButton("Нет", "lifestyle:vegan:no");
        var yesBtn = new TelegramClient.InlineButton("Да (веган/вегетарианец)", "lifestyle:vegan:yes");

        var kb = tg.inlineKeyboard(List.of(List.of(noBtn, yesBtn)));

        tg.sendMessage(chatId, text, kb);
    }

    private void sendPregnancyOptions(Long chatId, Long userId) {
        User user = userService.getUserOrThrow(userId);

        if (!"F".equalsIgnoreCase(user.getSex())) {
            tg.sendMessage(chatId, """
                            Параметр «Беременность» актуален только для женщин.
                            
                            В твоём профиле пол указан как: %s.
                            """.formatted(user.getSex() == null ? "не указан" : user.getSex()),
                    buildExtendedProfileKeyboard());
            return;
        }

        UserLifestyle lifestyle = userLifestyleService.findByUserId(userId).orElse(null);
        String current = (lifestyle != null && Boolean.TRUE.equals(lifestyle.getPregnant()))
                ? "да"
                : "нет";

        String text = """
                Настройка: беременность 🤰
                
                Текущее значение: %s
                
                Это влияет на рекомендации по ряду нутриентов
                (например, витамин D3, железо и др.).
                """.formatted(current);

        var noBtn = new TelegramClient.InlineButton("Нет", "lifestyle:pregnant:no");
        var yesBtn = new TelegramClient.InlineButton("Да", "lifestyle:pregnant:yes");

        var kb = tg.inlineKeyboard(List.of(List.of(noBtn, yesBtn)));

        tg.sendMessage(chatId, text, kb);
    }

    private void sendGeoInstructions(Long chatId, Long userId) {
        User user = userService.getUserOrThrow(userId);

        String hasGeo = (user.getGeoLat() != null && user.getGeoLon() != null)
                ? "уже указана"
                : "пока не указана";

        String text = """
                Геолокация 🌍
                
                В профиле геопозиция: %s.
                
                Мы будем использовать гео в будущем, чтобы учитывать:
                • количество солнца;
                • температуру;
                • климатические особенности региона.
                
                Чтобы обновить геолокацию — нажми кнопку ниже
                и поделись текущей геопозицией.
                """.formatted(hasGeo);

        KeyboardButton share = new KeyboardButton("📍 Поделиться геопозицией").requestLocation(true);
        KeyboardButton back = new KeyboardButton(NutrientsMenuButtons.BACK_TO_NUTRIENTS);

        ReplyKeyboardMarkup kb = new ReplyKeyboardMarkup(
                new KeyboardButton[]{share},
                new KeyboardButton[]{back}
        );
        kb.resizeKeyboard(true);
        kb.oneTimeKeyboard(false);
        kb.selective(true);

        tg.sendMessage(chatId, text, kb);
    }

    private void sendExtendedProfileMenu(Long chatId, Long userId) {
        UserLifestyle lifestyle = userLifestyleService.findByUserId(userId).orElse(null);

        String smoking = "не указано";
        if (lifestyle != null && lifestyle.getSmokePacksPerDay() != null) {
            double p = lifestyle.getSmokePacksPerDay();
            if (p == 0.0) smoking = "не курит";
            else if (p <= 0.2) smoking = "редко";
            else if (p <= 1.0) smoking = "до 1 пачки в день";
            else smoking = "больше пачки в день";
        }

        String vegan = (lifestyle != null && Boolean.TRUE.equals(lifestyle.getVegan()))
                ? "да"
                : "нет";

        String pregnant = (lifestyle != null && Boolean.TRUE.equals(lifestyle.getPregnant()))
                ? "да"
                : "нет / не актуально";

        String text = """
                Расширенный профиль 💊
                
                Курение: %s
                Веганство: %s
                Беременность: %s
                
                Эти параметры помогают точнее подбирать нутриенты
                и учитывать нагрузку на организм.
                
                Что хочешь настроить?
                """.formatted(smoking, vegan, pregnant);

        tg.sendMessage(chatId, text, buildExtendedProfileKeyboard());
    }

    private void sendStatsStub(Long chatId, NutrientStatsResult stats) {
        String period = "%s — %s".formatted(
                stats.fromDate(),
                stats.toDate()
        );

        StringBuilder sb = new StringBuilder();
        sb.append("Статистика по нутриентам (MVP) 📊\n\n")
                .append("Период: ").append(period).append("\n")
                .append("Тренировок в периоде: ").append(stats.workoutsCount()).append("\n\n")
                .append("Полноценная статистика по нутриентам появится позже.\n")
                .append("Планируем показывать:\n")
                .append("• суммарные потребности по ключевым нутриентам за неделю;\n")
                .append("• средние значения на одну тренировку;\n")
                .append("• дни с максимальной нагрузкой и дефицитом.\n");

        // На будущее: если вдруг мы начнём что-то считать раньше —
        // выводим черновой список нутриентов.
        if (stats.entries() != null && !stats.entries().isEmpty()) {
            sb.append("\nЧерновой расчёт:\n");
            for (var e : stats.entries()) {
                sb.append("• ")
                        .append(e.name()).append(" (").append(e.code()).append("): ")
                        .append(formatAmount(e.totalAmount())).append(" ").append(e.unit())
                        .append(" за период\n");
            }
        }

        tg.sendMessage(chatId, sb.toString(), buildNutrientsMenuKeyboard());
    }

    @Override
    public int getOrder() {
        // после WorkoutsMenuHandler (40), рядом с другими разделами
        return 50;
    }
}
