package com.nutribot.bot.workout;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CardioWorkoutAddFlowHandler implements BotUpdateHandler {

    private final WorkoutCreationStateStore creationStateStore;
    private final CardioCreationStateStore cardioStateStore;
    private final WorkoutService workoutService;
    private final UserService userService;
    private final TelegramClient tg;

    public void startNewCardioWorkout(Long chatId, Long userId, Workout workout) {
        // базовый стейт: есть черновик, тип CARDIO
        WorkoutCreationState baseState = WorkoutCreationState.builder()
                .workoutId(workout.getId())
                .type(WorkoutType.CARDIO)
                .currentExerciseId(null)
                .build();
        creationStateStore.setState(userId, baseState);

        // стейт кардио: ждём вид активности
        CardioCreationState cardioState = CardioCreationState.builder()
                .step(CardioCreationStep.ACTIVITY_TYPE)
                .build();
        cardioStateStore.setState(userId, cardioState);

        tg.sendMessage(chatId, """
                Добавим кардио-тренировку 🏃‍♂️
                
                Сначала укажи вид активности:
                Например: «Бег», «Велосипед», «Гребля», «Ходьба».
                """);
    }

    @Override
    public boolean canHandle(UpdateContext ctx) {
        Long telegramUserId = ctx.getTelegramUserId();
        if (telegramUserId == null) {
            return false;
        }

        String text = ctx.getText();
        if (text != null && text.startsWith("/")) {
            // глобальные команды отдаём другим хендлерам
            return false;
        }

        Long userId = userService.ensureUserByTelegramId(telegramUserId);
        Optional<WorkoutCreationState> baseOpt = creationStateStore.getState(userId);
        if (baseOpt.isEmpty()) {
            return false;
        }
        WorkoutCreationState baseState = baseOpt.get();
        if (baseState.getType() != WorkoutType.CARDIO) {
            return false;
        }

        // должен быть активный стейт кардио
        if (cardioStateStore.getState(userId).isEmpty()) {
            return false;
        }

        if (ctx.getCallbackQuery() != null) {
            return text != null && text.startsWith("workout:cardio:");
        } else {
            return text != null;
        }
    }

    @Override
    public void handle(UpdateContext ctx) {
        Long userId = userService.ensureUserByTelegramId(ctx.getTelegramUserId());
        Long chatId = ctx.getChatId();

        WorkoutCreationState baseState = creationStateStore.getState(userId).orElse(null);
        CardioCreationState cardioState = cardioStateStore.getState(userId).orElse(null);

        if (baseState == null || cardioState == null || baseState.getType() != WorkoutType.CARDIO) {
            tg.sendMessage(chatId, """
                    Что-то пошло не так с текущей кардио-тренировкой.
                    
                    Попробуй начать заново через:
                    «Тренировки» → «Добавить» → «Кардио».
                    """);
            creationStateStore.clear(userId);
            cardioStateStore.clear(userId);
            return;
        }

        if (ctx.getCallbackQuery() != null) {
            handleCallback(ctx, userId, chatId, baseState, cardioState);
        } else {
            handleText(ctx, userId, chatId, baseState, cardioState);
        }
    }

    private void handleText(UpdateContext ctx,
                            Long userId,
                            Long chatId,
                            WorkoutCreationState baseState,
                            CardioCreationState cardioState) {
        String input = ctx.getText().trim();

        switch (cardioState.getStep()) {
            case ACTIVITY_TYPE -> handleActivityTypeInput(chatId, userId, baseState, cardioState, input);
            case DURATION -> handleDurationInput(chatId, userId, baseState, cardioState, input);
            case DISTANCE -> handleDistanceInput(chatId, userId, baseState, cardioState, input);
            case INTENSITY -> handleIntensityTextInput(chatId, userId, baseState, cardioState, input);
            case SUMMARY -> {
                tg.sendMessage(chatId, """
                        Тренировка уже собрана.
                        
                        Выбери, пожалуйста, «Сохранить тренировку» или «Удалить»
                        с помощью кнопок ниже.
                        """);
            }
        }
    }

    private void handleCallback(UpdateContext ctx,
                                Long userId,
                                Long chatId,
                                WorkoutCreationState baseState,
                                CardioCreationState cardioState) {
        String data = ctx.getText();

        switch (data) {
            case "workout:cardio:intensity_low" -> handleIntensityButton(chatId, userId, baseState, cardioState, "LOW");
            case "workout:cardio:intensity_moderate" ->
                    handleIntensityButton(chatId, userId, baseState, cardioState, "MODERATE");
            case "workout:cardio:intensity_high" ->
                    handleIntensityButton(chatId, userId, baseState, cardioState, "HIGH");
            case "workout:cardio:save" -> handleSaveWorkout(chatId, userId, baseState);
            case "workout:cardio:delete" -> handleDeleteWorkout(chatId, userId, baseState);
            default -> tg.sendMessage(chatId,
                    "Не получилось распознать действие по кардио-тренировке. Попробуй ещё раз через раздел «Тренировки».");
        }
    }

    // ============ Шаг 1: вид активности ============

    private void handleActivityTypeInput(Long chatId,
                                         Long userId,
                                         WorkoutCreationState baseState,
                                         CardioCreationState cardioState,
                                         String input) {
        if (!isValidActivityName(input)) {
            tg.sendMessage(chatId, """
                    Кажется, это не очень похоже на вид активности 🙂
                    
                    Введи, пожалуйста, что-то вроде:
                    «Бег», «Велосипед», «Ходьба», «Гребля».
                    """);
            return;
        }
        String name = normalizeActivityName(input);

        CardioCreationState newState = cardioState.toBuilder()
                .activityType(name)
                .step(CardioCreationStep.DURATION)
                .build();
        cardioStateStore.setState(userId, newState);

        tg.sendMessage(chatId, """
                Вид активности записал: %s ✅
                
                Теперь укажи длительность тренировки в минутах.
                Например: 30, 45, 60.
                """.formatted(name));
    }

    private boolean isValidActivityName(String raw) {
        if (raw == null) return false;
        String s = raw.trim();
        if (s.length() < 2 || s.length() > 100) return false;
        return s.chars().anyMatch(Character::isLetter);
    }

    private String normalizeActivityName(String raw) {
        String s = raw.trim();
        if (s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ============ Шаг 2: длительность ============

    private void handleDurationInput(Long chatId,
                                     Long userId,
                                     WorkoutCreationState baseState,
                                     CardioCreationState cardioState,
                                     String input) {
        int minutes;
        try {
            minutes = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            sendDurationInvalid(chatId);
            return;
        }

        if (minutes < 5 || minutes > 600) { // todo от 5 минут или сделать от 1?
            sendDurationInvalid(chatId);
            return;
        }

        CardioCreationState newState = cardioState.toBuilder()
                .durationMin(minutes)
                .step(CardioCreationStep.DISTANCE)
                .build();
        cardioStateStore.setState(userId, newState);

        tg.sendMessage(chatId, """
                Длительность записал: %d минут ✅
                
                Теперь укажи дистанцию в километрах.
                Можно с запятой или точкой.
                
                Если дистанция не важна или неизвестна — введи 0.
                """.formatted(minutes));
    }

    private void sendDurationInvalid(Long chatId) {
        tg.sendMessage(chatId, """
                Нужно указать длительность в минутах (целое число от 5 до 600).
                Попробуй ещё раз 🙂
                """);
    }

    // ============ Шаг 3: дистанция ============

    private void handleDistanceInput(Long chatId,
                                     Long userId,
                                     WorkoutCreationState baseState,
                                     CardioCreationState cardioState,
                                     String input) {
        String s = input.replace(",", ".").trim();
        double distance;
        try {
            distance = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            sendDistanceInvalid(chatId);
            return;
        }

        if (distance < 0 || distance > 500) {
            sendDistanceInvalid(chatId);
            return;
        }

        CardioCreationState newState = cardioState.toBuilder()
                .distanceKm(distance)
                .step(CardioCreationStep.INTENSITY)
                .build();
        cardioStateStore.setState(userId, newState);

        sendIntensityQuestion(chatId, distance);
    }

    private void sendDistanceInvalid(Long chatId) {
        tg.sendMessage(chatId, """
                Нужно указать дистанцию числом в километрах (от 0 до 500).
                Можно использовать запятую или точку как разделитель.
                """);
    }

    private void sendIntensityQuestion(Long chatId, double distance) {
        String text = """
                Дистанция записана: %.2f км ✅
                
                Теперь оценим интенсивность тренировки.
                
                Можно:
                • выбрать Low / Moderate / High по ощущениям;
                • или ввести RPE (Rate of Perceived Exertion) числом от 1 до 10.
                
                Пример:
                • 3–4 — легко;
                • 5–7 — средне;
                • 8–9 — тяжело.
                """.formatted(distance);

        var lowBtn = new TelegramClient.InlineButton("Low", "workout:cardio:intensity_low");
        var modBtn = new TelegramClient.InlineButton("Moderate", "workout:cardio:intensity_moderate");
        var highBtn = new TelegramClient.InlineButton("High", "workout:cardio:intensity_high");

        var keyboard = tg.inlineKeyboard(
                List.of(
                        List.of(lowBtn, modBtn, highBtn)
                )
        );

        tg.sendMessage(chatId, text, keyboard);
    }

    // ============ Шаг 4: интенсивность — текст (RPE) ============

    private void handleIntensityTextInput(Long chatId,
                                          Long userId,
                                          WorkoutCreationState baseState,
                                          CardioCreationState cardioState,
                                          String input) {
        int rpe;
        try {
            rpe = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            sendIntensityInvalid(chatId);
            return;
        }

        if (rpe < 1 || rpe > 10) {
            sendIntensityInvalid(chatId);
            return;
        }

        String intensity = mapRpeToIntensity(rpe);

        CardioCreationState newState = cardioState.toBuilder()
                .rpe(rpe)
                .intensity(intensity)
                .step(CardioCreationStep.SUMMARY)
                .build();
        cardioStateStore.setState(userId, newState);

        saveCardioDetailsAndShowSummary(chatId, userId, baseState, newState);
    }

    private void sendIntensityInvalid(Long chatId) {
        tg.sendMessage(chatId, """
                Нужно указать RPE числом от 1 до 10.
                
                Чем выше число, тем субъективно тяжелее тренировка.
                Попробуй ещё раз 🙂
                """);
    }

    private String mapRpeToIntensity(int rpe) {
        if (rpe <= 3) return "LOW";
        if (rpe <= 7) return "MODERATE";
        return "HIGH";
    }

    // ============ Шаг 4: интенсивность — кнопки ============

    private void handleIntensityButton(Long chatId,
                                       Long userId,
                                       WorkoutCreationState baseState,
                                       CardioCreationState cardioState,
                                       String intensityCode) {
        String intensity = intensityCode;
        int rpeApprox = switch (intensityCode) {
            case "LOW" -> 3;
            case "MODERATE" -> 6;
            case "HIGH" -> 8;
            default -> 5;
        };

        CardioCreationState newState = cardioState.toBuilder()
                .intensity(intensity)
                .rpe(rpeApprox)
                .step(CardioCreationStep.SUMMARY)
                .build();
        cardioStateStore.setState(userId, newState);

        saveCardioDetailsAndShowSummary(chatId, userId, baseState, newState);
    }

    // ============ Сохранение деталей + итог ============

    private void saveCardioDetailsAndShowSummary(Long chatId,
                                                 Long userId,
                                                 WorkoutCreationState baseState,
                                                 CardioCreationState cardioState) {

        workoutService.fillCardioDetails(
                baseState.getWorkoutId(),
                cardioState.getActivityType(),
                cardioState.getDurationMin(),
                cardioState.getDistanceKm(),
                cardioState.getIntensity(),
                cardioState.getRpe()
        );

        WorkoutDetails details = workoutService.getWorkoutDetails(userId, baseState.getWorkoutId());
        CardioWorkoutDetails c = details.getCardioDetails();

        if (c == null) {
            tg.sendMessage(chatId,
                    "Не удалось собрать итог кардио-тренировки. Попробуй, пожалуйста, ещё раз начать тренировку.");
            return;
        }

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
        double dist = c.getDistanceKm() != null ? c.getDistanceKm() : 0.0;

        String summary = """
                Итог кардио-тренировки:
                
                Вид: %s
                Длительность: %d минут
                Дистанция: %.2f км
                Интенсивность: %s
                RPE: %s
                
                Что делаем с тренировкой?
                """.formatted(
                c.getActivityType(),
                c.getDurationMin(),
                dist,
                intensityLabel,
                rpeLabel
        );

        var saveBtn = new TelegramClient.InlineButton("Сохранить", "workout:cardio:save");
        var deleteBtn = new TelegramClient.InlineButton("Удалить", "workout:cardio:delete");
        var keyboard = tg.inlineKeyboard(List.of(List.of(saveBtn, deleteBtn)));

        tg.sendMessage(chatId, summary, keyboard);
    }

    // ============ Сохранить / удалить тренировку ============

    private void handleSaveWorkout(Long chatId, Long userId, WorkoutCreationState baseState) {
        workoutService.finishWorkout(baseState.getWorkoutId());
        creationStateStore.clear(userId);
        cardioStateStore.clear(userId);

        var nutrientsBtn = new TelegramClient.InlineButton(
                "Рассчитать нутриенты по этой тренировке",
                "workout:nutrients:" + baseState.getWorkoutId()
        );
        var mainMenuBtn = new TelegramClient.InlineButton("Главное меню", "menu:main");

        var keyboard = tg.inlineKeyboard(
                List.of(
                        List.of(nutrientsBtn),
                        List.of(mainMenuBtn)
                )
        );

        tg.sendMessage(chatId, """
                Тренировка сохранена ✅
                
                Позже по ней можно будет рассчитать нутриенты.
                """, keyboard);
    }

    private void handleDeleteWorkout(Long chatId, Long userId, WorkoutCreationState baseState) {
        workoutService.deleteWorkout(baseState.getWorkoutId());
        creationStateStore.clear(userId);
        cardioStateStore.clear(userId);

        tg.sendMessage(chatId,
                "Черновик тренировки удалён 🗑",
                tg.buildMainMenuKeyboard());
    }

    @Override
    public int getOrder() {
        // рядом со StrengthWorkoutAddFlowHandler (45), можно чуть позже
        return 46;
    }
}
