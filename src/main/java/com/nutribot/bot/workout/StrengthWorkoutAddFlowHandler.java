package com.nutribot.bot.workout;

import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Флоу добавления силовой тренировки:
 * - название упражнения;
 * - ввод подходов (вес, повторы[, RIR]);
 * - новое упражнение / завершить тренировку;
 * - итог + сохранить / удалить.
 */
@Component
@RequiredArgsConstructor
public class StrengthWorkoutAddFlowHandler implements BotUpdateHandler {

    private final WorkoutCreationStateStore creationStateStore;
    private final WorkoutService workoutService;
    private final StrengthExerciseRepository strengthExerciseRepository;
    private final UserService userService;
    private final TelegramClient tg;

    // record для удобного парсинга подхода
    private record ParsedSet(double weight, int reps, Integer rir) {
    }

    /**
     * Запуск флоу, когда пользователь выбрал тип "Силовая".
     */
    public void startNewStrengthWorkout(Long chatId, Long userId, Workout workout) {
        WorkoutCreationState state = WorkoutCreationState.builder()
                .workoutId(workout.getId())
                .type(WorkoutType.STRENGTH)
                .currentExerciseId(null)
                .build();
        creationStateStore.setState(userId, state);

        tg.sendMessage(chatId, """
                Отлично, заведём силовую тренировку 💪
                
                Сначала введи название упражнения.
                Например: «Жим лёжа», «Приседания», «Тяга в наклоне».
                """);
    }

    @Override
    public boolean canHandle(UpdateContext ctx) {
        Long telegramUserId = ctx.getTelegramUserId();
        if (telegramUserId == null) {
            return false;
        }

        String text = ctx.getText();
        // команды типа /start, /help, /cancel отдаем другим
        if (text != null && text.startsWith("/")) {
            return false;
        }

        Long userId = userService.ensureUserByTelegramId(telegramUserId);
        Optional<WorkoutCreationState> stateOpt = creationStateStore.getState(userId);
        if (stateOpt.isEmpty()) {
            return false;
        }

        WorkoutCreationState state = stateOpt.get();
        if (state.getType() != WorkoutType.STRENGTH) {
            return false;
        }

        // обработка:
        // - текст (имя упражнения / подходы),
        // - callback'и вида workout:strength:...
        if (ctx.getCallbackQuery() != null) {
            return text != null && text.startsWith("workout:strength:");
        } else {
            return text != null;
        }
    }

    @Override
    public void handle(UpdateContext ctx) {
        Long userId = userService.ensureUserByTelegramId(ctx.getTelegramUserId());
        Long chatId = ctx.getChatId();

        WorkoutCreationState state = creationStateStore.getState(userId)
                .orElseThrow(); // canHandle гарантирует, что он есть

        if (ctx.getCallbackQuery() != null) {
            handleCallback(ctx, userId, chatId, state);
        } else {
            handleText(ctx, userId, chatId, state);
        }
    }

    private void handleCallback(UpdateContext ctx, Long userId, Long chatId, WorkoutCreationState state) {
        String data = ctx.getText(); // callback_data

        switch (data) {
            case "workout:strength:new_exercise" -> handleNewExerciseButton(chatId, userId, state);
            case "workout:strength:finish" -> handleFinishButton(chatId, userId, state);
            case "workout:strength:save" -> handleSaveWorkout(chatId, userId, state);
            case "workout:strength:delete" -> handleDeleteWorkout(chatId, userId, state);
            default -> tg.sendMessage(chatId,
                    "Не получилось распознать действие по тренировке. Попробуй ещё раз через раздел «Тренировки».");
        }
    }

    private void handleText(UpdateContext ctx, Long userId, Long chatId, WorkoutCreationState state) {
        String input = ctx.getText().trim();

        if (state.getCurrentExerciseId() == null) {
            // Ждём название упражнения
            handleExerciseNameInput(chatId, userId, state, input);
        } else {
            // Ждём подход: вес,повторы[,RIR]
            handleSetInput(chatId, userId, state, input);
        }
    }

    // ==================== имя упражнения ====================

    private void handleExerciseNameInput(Long chatId, Long userId, WorkoutCreationState state, String input) {
        if (!isValidExerciseName(input)) {
            tg.sendMessage(chatId,
                    """
                            Кажется, это не очень похоже на название упражнения 🙂
                            
                            Введи, пожалуйста, название от 2 до 100 символов.
                            Например: «Жим лёжа», «Приседания» или «Тяга в наклоне».
                            """);
            return;
        }

        String name = input.trim();
        StrengthExercise exercise = workoutService.addStrengthExercise(state.getWorkoutId(), name);

        // обновляем state: привязываем текущее упражнение
        WorkoutCreationState newState = WorkoutCreationState.builder()
                .workoutId(state.getWorkoutId())
                .type(state.getType())
                .currentExerciseId(exercise.getId())
                .build();
        creationStateStore.setState(userId, newState);

        sendRirHelpAndFirstSetPrompt(chatId, exercise.getName());
    }

    private boolean isValidExerciseName(String raw) {
        if (raw == null) return false;
        String s = raw.trim();
        if (s.length() < 2 || s.length() > 100) return false;
        return s.chars().anyMatch(Character::isLetter);
    }

    private void sendRirHelpAndFirstSetPrompt(Long chatId, String exerciseName) {
        String text = """
                Упражнение «%s» добавлено ✅
                
                Теперь будем записывать подходы.
                
                Формат ввода: вес,повторы[,RIR]
                
                Что такое RIR (reps in reserve):
                • 0 — подход до отказа;
                • 1–2 — очень тяжело, но есть небольшой запас;
                • 3–4 — умеренно тяжело, запас заметный.
                
                Примеры:
                80,10,2
                60 8
                
                Разделители можно ставить как пробел, запятую, точку или дефис.
                
                Введи первый подход для «%s»
                или воспользуйся кнопками ниже:
                """.formatted(exerciseName, exerciseName);

        var newExerciseBtn = new TelegramClient.InlineButton("Новое упражнение", "workout:strength:new_exercise");
        var finishBtn = new TelegramClient.InlineButton("Завершить тренировку", "workout:strength:finish");

        var keyboard = tg.inlineKeyboard(
                List.of(
                        List.of(newExerciseBtn, finishBtn)
                )
        );

        tg.sendMessage(chatId, text, keyboard);
    }

    // ==================== подходы ====================

    private void handleSetInput(Long chatId, Long userId, WorkoutCreationState state, String input) {
        ParsedSet parsed = tryParseSet(input);
        if (parsed == null) {
            sendSetInvalid(chatId);
            return;
        }

        Long exerciseId = state.getCurrentExerciseId();
        StrengthExercise exercise = strengthExerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalStateException("Exercise not found: " + exerciseId));

        StrengthSet set = workoutService.addStrengthSet(
                exerciseId,
                parsed.weight(),
                parsed.reps(),
                parsed.rir()
        );

        int setNumber = set.getOrderIndex() != null ? set.getOrderIndex() : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("Записал подход #").append(setNumber)
                .append(" для «").append(exercise.getName()).append("»:\n")
                .append(parsed.weight()).append(" кг × ").append(parsed.reps()).append(" повторений");
        if (parsed.rir() != null) {
            sb.append(", RIR ").append(parsed.rir());
        }
        sb.append(".\n\n")
                .append("Введи следующий подход для этого упражнения\n")
                .append("или используй кнопки ниже:");

        var newExerciseBtn = new TelegramClient.InlineButton("Новое упражнение", "workout:strength:new_exercise");
        var finishBtn = new TelegramClient.InlineButton("Завершить тренировку", "workout:strength:finish");
        var keyboard = tg.inlineKeyboard(List.of(List.of(newExerciseBtn, finishBtn)));

        tg.sendMessage(chatId, sb.toString(), keyboard);
    }

    private ParsedSet tryParseSet(String input) {
        if (input == null) return null;
        String normalized = input.trim();
        if (normalized.isEmpty()) return null;

        // разделители: пробел, запятая, точка, точка с запятой, дефис, слэш
        String[] tokens = normalized.split("[\\s,.;/\\-]+");
        if (tokens.length < 2 || tokens.length > 3) {
            return null;
        }

        try {
            double weight = Double.parseDouble(tokens[0]);
            int reps = Integer.parseInt(tokens[1]);
            Integer rir = null;
            if (tokens.length == 3) {
                rir = Integer.valueOf(tokens[2]);
            }

            if (weight < 0 || reps <= 0) {
                return null;
            }
            if (rir != null && (rir < 0 || rir > 8)) {
                return null;
            }

            return new ParsedSet(weight, reps, rir);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void sendSetInvalid(Long chatId) {
        tg.sendMessage(chatId, """
                Не получилось распознать подход 😔
                
                Формат: вес,повторы[,RIR]
                
                Примеры:
                80,10,2
                60 8
                
                Разделители можно ставить как пробел, запятую, точку или дефис.
                """);
    }

    // ==================== кнопка «Новое упражнение» ====================

    private void handleNewExerciseButton(Long chatId, Long userId, WorkoutCreationState state) {
        // просто сбрасываем текущее упражнение — дальше ждём название
        WorkoutCreationState newState = WorkoutCreationState.builder()
                .workoutId(state.getWorkoutId())
                .type(state.getType())
                .currentExerciseId(null)
                .build();
        creationStateStore.setState(userId, newState);

        tg.sendMessage(chatId, """
                Ок, добавим новое упражнение.
                
                Введи его название:
                """);
    }

    // ==================== кнопка «Завершить тренировку» ====================

    private void handleFinishButton(Long chatId, Long userId, WorkoutCreationState state) {
        WorkoutDetails details = workoutService.getWorkoutDetails(userId, state.getWorkoutId());

        StringBuilder sb = new StringBuilder();
        sb.append("Итог силовой тренировки:\n\n");

        List<StrengthExerciseWithSets> blocks = details.getStrengthExercises();
        if (blocks.isEmpty()) {
            sb.append("Пока нет ни одного упражнения или подхода.\n\n");
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

        sb.append("Что делаем с тренировкой?");

        // todo разные варианты текста в saveBtn и вывода в keyboard: либо на одной строке, либо на двух
        var saveBtn = new TelegramClient.InlineButton("Сохранить", "workout:strength:save");
//        var saveBtn = new TelegramClient.InlineButton("Сохранить тренировку", "workout:strength:save");
        var deleteBtn = new TelegramClient.InlineButton("Удалить", "workout:strength:delete");

        var keyboard = tg.inlineKeyboard(List.of(List.of(saveBtn, deleteBtn)));
//        var keyboard = tg.inlineKeyboard(List.of(List.of(saveBtn), List.of(deleteBtn)));

        tg.sendMessage(chatId, sb.toString(), keyboard);
    }

    // ==================== сохранить / удалить ====================

    private void handleSaveWorkout(Long chatId, Long userId, WorkoutCreationState state) {
        workoutService.finishWorkout(state.getWorkoutId());
        creationStateStore.clear(userId);

        var nutrientsBtn = new TelegramClient.InlineButton(
                "Рассчитать нутриенты по этой тренировке",
                "workout:nutrients:" + state.getWorkoutId()
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

    private void handleDeleteWorkout(Long chatId, Long userId, WorkoutCreationState state) {
        workoutService.deleteWorkout(state.getWorkoutId());
        creationStateStore.clear(userId);

        tg.sendMessage(chatId,
                "Черновик тренировки удалён 🗑",
                tg.buildMainMenuKeyboard());
    }

    @Override
    public int getOrder() {
        // после WorkoutsMenuHandler (40), до fallback-эхо
        return 45;
    }
}
