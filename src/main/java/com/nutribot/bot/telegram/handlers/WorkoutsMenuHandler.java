package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.onboarding.OnboardingGate;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.telegram.menu.MainMenuButtons;
import com.nutribot.bot.telegram.menu.WorkoutsMenuButtons;
import com.nutribot.bot.user.UserService;
import com.nutribot.bot.workout.Workout;
import com.nutribot.bot.workout.WorkoutCreationStateStore;
import com.nutribot.bot.workout.WorkoutService;
import com.nutribot.bot.workout.WorkoutType;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.nutribot.bot.workout.StrengthWorkoutAddFlowHandler;
import com.nutribot.bot.workout.CardioWorkoutAddFlowHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.nutribot.bot.workout.WorkoutDetails;
import com.nutribot.bot.workout.StrengthExerciseWithSets;
import com.nutribot.bot.workout.StrengthExercise;
import com.nutribot.bot.workout.StrengthSet;
import com.nutribot.bot.workout.CardioWorkoutDetails;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Раздел «Тренировки»:
 * - вход из главного меню;
 * - меню: Добавить / Мои тренировки / Главное меню;
 * - выбор типа тренировки для флоу «Добавить».
 */
@Component
@RequiredArgsConstructor
public class WorkoutsMenuHandler implements BotUpdateHandler {

    private static final int PAGE_SIZE = 5;

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final OnboardingGate onboardingGate;
    private final UserService userService;
    private final WorkoutService workoutService;
    private final WorkoutCreationStateStore creationStateStore;
    private final TelegramClient tg;
    private final StrengthWorkoutAddFlowHandler strengthWorkoutAddFlowHandler;
    private final CardioWorkoutAddFlowHandler cardioWorkoutAddFlowHandler;


    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();

        if (ctx.getCallbackQuery() != null && text != null) {
            // inline-кнопки выбора типа
            if (text.startsWith("workout:type_")) {
                return true;
            }
            // список / пагинация / карточка / удаление
            if (text.startsWith("workouts:list:")
                    || text.startsWith("workouts:card:")
                    || text.startsWith("workouts:delete:")) {
                return true;
            }
            return false;
        }

        if (text == null) {
            return false;
        }

        return text.equals(MainMenuButtons.WORKOUTS)
                || text.equals(WorkoutsMenuButtons.ADD)
                || text.equals(WorkoutsMenuButtons.MY_WORKOUTS)
                || text.equals(WorkoutsMenuButtons.BACK_TO_MAIN);
    }


    @Override
    public void handle(UpdateContext ctx) {
        // проверка онбординга для любого захода в раздел
        if (!onboardingGate.ensureOnboardingCompleted(ctx)) {
            return;
        }

        Long userId = userService.ensureUserByTelegramId(ctx.getTelegramUserId());
        Long chatId = ctx.getChatId();
        String text = ctx.getText();

        // === callback'и ===
        if (ctx.getCallbackQuery() != null && text != null) {

            if (text.startsWith("workout:type_")) {
                handleWorkoutTypeSelected(ctx, userId, chatId, text);
                return;
            }

            if (text.startsWith("workouts:list:")) {
                handleWorkoutsListCallback(chatId, userId, text);
                return;
            }

            if (text.startsWith("workouts:card:")) {
                handleWorkoutCardCallback(chatId, userId, text);
                return;
            }

            if (text.startsWith("workouts:delete:")) {
                handleWorkoutDeleteCallback(chatId, userId, text);
                return;
            }

            return;
        }

        // === текстовые сообщения ===

        if (MainMenuButtons.WORKOUTS.equals(text)) {
            tg.sendMessage(chatId,
                    "Раздел «Тренировки». Что дальше?",
                    buildWorkoutsMenuKeyboard());
            return;
        }

        if (WorkoutsMenuButtons.ADD.equals(text)) {
            // старт флоу "Добавить": выбор типа тренировки
            sendWorkoutTypeChoice(chatId);
            return;
        }

        if (WorkoutsMenuButtons.MY_WORKOUTS.equals(text)) {
            // показываем первую страницу "Мои тренировки"
            sendWorkoutsPage(chatId, userId, 0);
            return;
        }

        if (WorkoutsMenuButtons.BACK_TO_MAIN.equals(text)) {
            tg.sendMainMenu(chatId);
        }
    }

    private void sendWorkoutTypeChoice(Long chatId) {
        String text = """
                Какую тренировку добавляем?
                
                Выбери тип:
                """;

        var strengthBtn = new TelegramClient.InlineButton("Силовая", "workout:type_strength");
        var cardioBtn = new TelegramClient.InlineButton("Кардио", "workout:type_cardio");

        var keyboard = tg.inlineKeyboard(
                List.of(
                        List.of(strengthBtn),
                        List.of(cardioBtn)
                )
        );

        tg.sendMessage(chatId, text, keyboard);
    }

    private void handleWorkoutTypeSelected(UpdateContext ctx, Long userId, Long chatId, String data) {
        WorkoutType type = switch (data) {
            case "workout:type_strength" -> WorkoutType.STRENGTH;
            case "workout:type_cardio" -> WorkoutType.CARDIO;
            default -> {
                tg.sendMessage(chatId,
                        "Не получилось распознать тип тренировки. Попробуй ещё раз через меню «Тренировки».");
                yield null;
            }
        };

        if (type == null) {
            return;
        }

        // создаём черновик тренировки нужного типа
        Workout workout = workoutService.createWorkout(userId, type);

        if (type == WorkoutType.STRENGTH) {
            // флоу силовой
            strengthWorkoutAddFlowHandler.startNewStrengthWorkout(chatId, userId, workout);
        } else {
            // флоу кардио
            cardioWorkoutAddFlowHandler.startNewCardioWorkout(chatId, userId, workout);
        }
    }


    private ReplyKeyboardMarkup buildWorkoutsMenuKeyboard() {
        KeyboardButton add = new KeyboardButton(WorkoutsMenuButtons.ADD);
        KeyboardButton my = new KeyboardButton(WorkoutsMenuButtons.MY_WORKOUTS);
        KeyboardButton back = new KeyboardButton(WorkoutsMenuButtons.BACK_TO_MAIN);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[]{add, my},
                new KeyboardButton[]{back}
        );
        keyboard.resizeKeyboard(true);
        keyboard.oneTimeKeyboard(false);
        keyboard.selective(true);

        return keyboard;
    }

    private void sendWorkoutsPage(Long chatId, Long userId, int page) {
        if (page < 0) {
            page = 0;
        }

        List<Workout> slice = workoutService.listUserWorkouts(userId, page, PAGE_SIZE);

        if (page == 0 && slice.isEmpty()) {
            tg.sendMessage(chatId,
                    """
                            У тебя ещё нет сохранённых тренировок 📭
                            
                            Можно добавить первую через:
                            «Тренировки» → «Добавить».
                            """,
                    buildWorkoutsMenuKeyboard());
            return;
        }

        boolean hasNext = slice.size() > PAGE_SIZE;
        List<Workout> workouts = slice.size() > PAGE_SIZE
                ? slice.subList(0, PAGE_SIZE)
                : slice;

        StringBuilder sb = new StringBuilder();
        sb.append("Мои тренировки (страница ").append(page + 1).append(")\n\n");

        if (workouts.isEmpty()) {
            sb.append("На этой странице нет тренировок.\n");
        }

        List<List<TelegramClient.InlineButton>> rows = new ArrayList<>();

        for (Workout w : workouts) {
            String label = formatWorkoutLabel(w);
            String data = "workouts:card:" + w.getId() + ":" + page;
            rows.add(List.of(new TelegramClient.InlineButton(label, data)));
        }

        // навигация
        boolean hasPrev = page > 0;
        List<TelegramClient.InlineButton> navRow = new ArrayList<>();
        if (hasPrev) {
            navRow.add(new TelegramClient.InlineButton("⬅️ Назад", "workouts:list:" + (page - 1)));
        }
        if (hasNext) {
            navRow.add(new TelegramClient.InlineButton("Вперёд ➡️", "workouts:list:" + (page + 1)));
        }
        if (!navRow.isEmpty()) {
            rows.add(navRow);
        }

        // кнопка "Главное меню"
        rows.add(List.of(new TelegramClient.InlineButton("Главное меню", "menu:main")));

        var keyboard = tg.inlineKeyboard(rows);
        tg.sendMessage(chatId, sb.toString(), keyboard);
    }

    private String formatWorkoutLabel(Workout w) {
        String typeLabel = (w.getType() == WorkoutType.STRENGTH) ? "Силовая" : "Кардио";
        String dt;
        if (w.getStartedAt() != null) {
            dt = w.getStartedAt().toLocalDateTime().format(DATE_TIME_FMT);
        } else {
            dt = "без даты";
        }
        return dt + " • " + typeLabel;
    }

    private void handleWorkoutsListCallback(Long chatId, Long userId, String data) {
        // формат: workouts:list:{page}
        String[] parts = data.split(":");
        if (parts.length < 3) {
            tg.sendMessage(chatId,
                    "Не получилось открыть список тренировок. Попробуй снова через меню «Тренировки».");
            return;
        }
        int page;
        try {
            page = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            page = 0;
        }

        sendWorkoutsPage(chatId, userId, page);
    }

    private void handleWorkoutCardCallback(Long chatId, Long userId, String data) {
        // формат: workouts:card:{workoutId}:{page}
        String[] parts = data.split(":");
        if (parts.length < 3) {
            tg.sendMessage(chatId,
                    "Не удалось найти тренировку. Попробуй открыть список через меню «Тренировки».");
            return;
        }

        Long workoutId;
        int page = 0;
        try {
            workoutId = Long.parseLong(parts[2]);
            if (parts.length >= 4) {
                page = Integer.parseInt(parts[3]);
            }
        } catch (NumberFormatException e) {
            tg.sendMessage(chatId,
                    "Не удалось распознать тренировку. Попробуй ещё раз через «Мои тренировки».");
            return;
        }

        WorkoutDetails details;
        try {
            details = workoutService.getWorkoutDetails(userId, workoutId);
        } catch (Exception e) {
            tg.sendMessage(chatId,
                    "Не удалось загрузить данные тренировки. Возможно, она была удалена.");
            return;
        }

        Workout w = details.getWorkout();
        String header = (w.getType() == WorkoutType.STRENGTH)
                ? "Силовая тренировка"
                : "Кардио-тренировка";

        String dt = (w.getStartedAt() != null)
                ? w.getStartedAt().toLocalDateTime().format(DATE_TIME_FMT)
                : "дата не указана";

        StringBuilder sb = new StringBuilder();
        sb.append(header).append(" #").append(w.getId()).append("\n\n");
        sb.append("Дата: ").append(dt).append("\n\n");

        if (w.getType() == WorkoutType.STRENGTH) {
            List<StrengthExerciseWithSets> blocks = details.getStrengthExercises();
            if (blocks.isEmpty()) {
                sb.append("Пока нет упражнений или подходов.\n");
            } else {
                int exIndex = 1;
                for (StrengthExerciseWithSets block : blocks) {
                    StrengthExercise ex = block.getExercise();
                    sb.append(exIndex++).append(". ").append(ex.getName()).append("\n");

                    List<StrengthSet> sets = block.getSets();
                    int setIndex = 1;
                    for (StrengthSet s : sets) {
                        sb.append("   #").append(setIndex++).append(": ")
                                .append(s.getWeight()).append(" кг × ")
                                .append(s.getReps()).append(" повторений");
                        if (s.getRir() != null) {
                            sb.append(", RIR ").append(s.getRir());
                        }
                        sb.append("\n");
                    }
                    sb.append("\n");
                }
            }
        } else {
            CardioWorkoutDetails c = details.getCardioDetails();
            if (c == null) {
                sb.append("Детали кардио-тренировки не найдены.\n");
            } else {
                double dist = c.getDistanceKm() != null ? c.getDistanceKm() : 0.0;
                String intensityLabel;
                if (c.getIntensity() == null) {
                    intensityLabel = "не указана";
                } else {
                    intensityLabel = switch (c.getIntensity()) {
                        case "LOW" -> "Low (лёгкая)";
                        case "MODERATE" -> "Moderate (средняя)";
                        case "HIGH" -> "High (тяжёлая)";
                        default -> c.getIntensity();
                    };
                }
                String rpeLabel = (c.getRpe() != null) ? String.valueOf(c.getRpe()) : "не указано";

                sb.append("Вид: ").append(c.getActivityType()).append("\n")
                        .append("Длительность: ").append(c.getDurationMin()).append(" минут\n")
                        .append("Дистанция: ").append(String.format("%.2f", dist)).append(" км\n")
                        .append("Интенсивность: ").append(intensityLabel).append("\n")
                        .append("RPE: ").append(rpeLabel).append("\n");
            }
        }

        sb.append("\nЧто дальше?");

        List<List<TelegramClient.InlineButton>> rows = new ArrayList<>();

        // расчёт нутриентов
        rows.add(List.of(new TelegramClient.InlineButton(
                "Рассчитать нутриенты по этой тренировке",
                "workout:nutrients:" + w.getId()
        )));

        // удалить + назад
        List<TelegramClient.InlineButton> secondRow = new ArrayList<>();
        secondRow.add(new TelegramClient.InlineButton(
                "Удалить тренировку",
                "workouts:delete:" + w.getId() + ":" + page
        ));
        secondRow.add(new TelegramClient.InlineButton(
                "⬅️ К списку",
                "workouts:list:" + page
        ));
        rows.add(secondRow);

        var keyboard = tg.inlineKeyboard(rows);
        tg.sendMessage(chatId, sb.toString(), keyboard);
    }

    private void handleWorkoutDeleteCallback(Long chatId, Long userId, String data) {
        // формат: workouts:delete:{workoutId}:{page}
        String[] parts = data.split(":");
        if (parts.length < 3) {
            tg.sendMessage(chatId,
                    "Не удалось удалить тренировку. Попробуй ещё раз через «Мои тренировки».");
            return;
        }

        Long workoutId;
        int page = 0;
        try {
            workoutId = Long.parseLong(parts[2]);
            if (parts.length >= 4) {
                page = Integer.parseInt(parts[3]);
            }
        } catch (NumberFormatException e) {
            tg.sendMessage(chatId,
                    "Не удалось распознать тренировку. Попробуй ещё раз через «Мои тренировки».");
            return;
        }

        try {
            workoutService.deleteWorkout(workoutId);
        } catch (Exception e) {
            tg.sendMessage(chatId,
                    "Не получилось удалить тренировку (возможно, она уже удалена).");
            // всё равно попробуем обновить список
        }

        tg.sendMessage(chatId, "Тренировка удалена 🗑");

        // показываем актуальный список
        sendWorkoutsPage(chatId, userId, page);
    }


    @Override
    public int getOrder() {
        // после профиля (30/31), до нутриентов
        return 40;
    }
}
