package com.nutribot.bot.nutrition;

import com.nutribot.bot.workout.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;

/**
 * Сервис статистики по нутриентам.
 * <p>
 * На шаге 4.4 это stub:
 * - даёт период (последние 7 дней),
 * - отдаёт пустой список нутриентов и 0 тренировок.
 * <p>
 * На следующих шагах сюда добавим реальный расчёт:
 * - пробег по тренировкам за период,
 * - суммирование результатов NutrientCalculatorService.
 */
@Service
@RequiredArgsConstructor
public class NutrientStatsService {

    private final WorkoutService workoutService;
    private final NutrientCalculatorService nutrientCalculatorService;

    @Transactional(readOnly = true)
    public NutrientStatsResult getWeeklyStats(Long userId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate from = today.minusDays(6);

        // TODO: реальная статистика:
        //  1) найти тренировки за период,
        //  2) для каждой вызвать calculateForWorkout(userId, workoutId),
        //  3) агрегировать по коду нутриента.

        return NutrientStatsResult.builder()
                .fromDate(from)
                .toDate(today)
                .workoutsCount(0)
                .entries(Collections.emptyList())
                .build();
    }
}
