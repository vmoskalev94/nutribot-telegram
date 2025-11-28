package com.nutribot.bot.workout.tss;

import com.nutribot.bot.workout.CardioWorkoutDetails;
import com.nutribot.bot.workout.WorkoutDetails;
import com.nutribot.bot.workout.WorkoutType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис расчёта метрик нагрузки: SS и TSS.
 * <p>
 * SS (Strength Score) — для силовых тренировок.
 * TSS (Training Stress Score) — для кардио тренировок.
 */
@Service
@RequiredArgsConstructor
public class TssService {

    private final List<TssCalculator> calculators;
    private final SsCalculator ssCalculator;
    private final CardioTssCalculator cardioTssCalculator;

    /**
     * Рассчитывает TSS (старый метод для обратной совместимости).
     * Для кардио использует дефолтный возраст.
     */
    public double calculateTotalTss(WorkoutDetails details) {
        if (details == null || details.getWorkout() == null) {
            return 0.0;
        }

        WorkoutType type = details.getWorkout().getType();

        return calculators.stream()
                .filter(c -> c.supports(type))
                .findFirst()
                .map(c -> c.calculate(details))
                .orElse(0.0);
    }

    /**
     * Рассчитывает TSS для кардио с учётом возраста пользователя.
     *
     * @param details детали тренировки
     * @param age     возраст пользователя
     * @return TSS (0 для силовых)
     */
    public double calculateTss(WorkoutDetails details, Integer age) {
        if (details == null || details.getWorkout() == null) {
            return 0.0;
        }

        WorkoutType type = details.getWorkout().getType();

        if (type == WorkoutType.CARDIO) {
            CardioWorkoutDetails cardio = details.getCardioDetails();
            if (cardio == null || cardio.getDurationMin() == null) {
                return 0.0;
            }
            return cardioTssCalculator.calculate(cardio.getDurationMin(), age);
        }

        // Для силовых TSS = 0 (используем SS)
        return 0.0;
    }

    /**
     * Рассчитывает SS (Strength Score) для силовых тренировок.
     *
     * @param details детали тренировки
     * @return SS (0 для кардио)
     */
    public double calculateSs(WorkoutDetails details) {
        if (details == null || details.getWorkout() == null) {
            return 0.0;
        }

        WorkoutType type = details.getWorkout().getType();

        if (type == WorkoutType.STRENGTH) {
            return ssCalculator.calculate(details);
        }

        // Для кардио SS = 0
        return 0.0;
    }
}
