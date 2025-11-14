package com.nutribot.bot.telegram.handlers;

import com.nutribot.bot.nutrition.*;
import com.nutribot.bot.telegram.client.TelegramClient;
import com.nutribot.bot.telegram.dispatcher.BotUpdateHandler;
import com.nutribot.bot.telegram.dispatcher.UpdateContext;
import com.nutribot.bot.user.User;
import com.nutribot.bot.user.UserService;
import com.nutribot.bot.workout.WorkoutDetails;
import com.nutribot.bot.workout.WorkoutService;
import com.nutribot.bot.workout.WorkoutType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkoutNutrientsHandler implements BotUpdateHandler {

    private final UserService userService;
    private final NutrientExplainProperties props;
    private final WorkoutService workoutService;
    private final NutrientCalculatorService nutrientCalculatorService;
    private final NutrientExplanationService nutrientExplanationService;
    private final TelegramClient tg;

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public boolean canHandle(UpdateContext ctx) {
        String text = ctx.getText();
        return ctx.getCallbackQuery() != null
                && text != null
                && text.startsWith("workout:nutrients:");
    }

    @Override
    public void handle(UpdateContext ctx) {
        Long chatId = ctx.getChatId();
        Long telegramUserId = ctx.getTelegramUserId();
        if (chatId == null || telegramUserId == null) {
            return;
        }

        String data = ctx.getText();
        Long workoutId = parseWorkoutId(data);
        if (workoutId == null) {
            tg.sendMessage(chatId,
                    "Не удалось распознать тренировку для расчёта нутриентов.");
            return;
        }

        Long userId = userService.ensureUserByTelegramId(telegramUserId);

        WorkoutDetails details;
        try {
            details = workoutService.getWorkoutDetails(userId, workoutId);
        } catch (Exception e) {
            tg.sendMessage(chatId,
                    "Не удалось загрузить данные тренировки. Возможно, она была удалена.");
            return;
        }

        NutrientCalculationResult calc =
                nutrientCalculatorService.calculateWithContext(userId, workoutId);
        List<NutrientRecommendation> recs = calc.recommendations();

        var workout = details.getWorkout();
        String typeLabel = (workout.getType() == WorkoutType.STRENGTH)
                ? "Силовая тренировка"
                : "Кардио-тренировка";

        String dt = (workout.getStartedAt() != null)
                ? workout.getStartedAt().toLocalDateTime().format(DATE_TIME_FMT)
                : "дата не указана";

        StringBuilder sb = new StringBuilder();
        sb.append("Расчёт нутриентов по выбранной тренировке:\n\n");
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

        // inline-кнопка "Пояснить расчёт"
        if (props.isUserVisible()) {
            var explainBtn = new TelegramClient.InlineButton(
                    "Пояснить расчёт",
                    "nutr:explain:" + workoutId
            );
            var kb = tg.inlineKeyboard(List.of(List.of(explainBtn)));
            tg.sendMessage(chatId, sb.toString(), kb);
        } else {
            tg.sendMessage(chatId, sb.toString());
        }
//        var explainBtn = new TelegramClient.InlineButton(
//                "Пояснить расчёт",
//                "nutr:explain:" + workoutId
//        );
//        var kb = tg.inlineKeyboard(List.of(List.of(explainBtn)));
//
//        tg.sendMessage(chatId, sb.toString(), kb);

        // Если у пользователя включён подробный режим — сразу досылаем лог
        User user = userService.getUserOrThrow(userId);
        boolean verbose = Boolean.TRUE.equals(user.getNutrientVerbose());
        if (verbose) {
            String explanation = nutrientExplanationService.buildExplanation(calc);
            tg.sendMessage(chatId, explanation);
        }
    }

    private Long parseWorkoutId(String data) {
        if (data == null) {
            return null;
        }
        String[] parts = data.split(":");
        if (parts.length < 3) {
            return null;
        }
        try {
            return Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatAmount(double value) {
        if (value < 10) {
            return String.format("%.1f", value);
        }
        return String.format("%.0f", value);
    }

    @Override
    public int getOrder() {
        // После хендлеров тренировок, до explain/verbose
        return 600;
    }
}
