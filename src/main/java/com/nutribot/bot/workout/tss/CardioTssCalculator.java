package com.nutribot.bot.workout.tss;

import com.nutribot.bot.workout.CardioWorkoutDetails;
import com.nutribot.bot.workout.WorkoutDetails;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

/**
 * Калькулятор TSS (Training Stress Score) для кардио тренировок.
 * <p>
 * Формула:
 * TSS = (duration_min / 60) × (HR_avg / HR_threshold) × 100
 * <p>
 * Где:
 * - HR_threshold = (220 - возраст) × 0.85
 * - HR_avg = 70 (хардкод на MVP)
 * <p>
 * TSS используется для аэробно-зависимых нутриентов: Fe, D3, Ca, B12 и др.
 */
@Component
public class CardioTssCalculator implements TssCalculator {

    /**
     * MVP-константа: средний пульс.
     * В будущем будет получаться с устройств или вводиться пользователем.
     */
    private static final double DEFAULT_HR_AVG = 70.0;

    /**
     * Коэффициент для расчёта HR_threshold от максимального пульса.
     */
    private static final double HR_THRESHOLD_COEFFICIENT = 0.85;

    @Override
    public boolean supports(WorkoutType type) {
        return type == WorkoutType.CARDIO;
    }

    @Override
    public double calculate(WorkoutDetails details) {
        CardioWorkoutDetails c = details.getCardioDetails();
        if (c == null) {
            return 0.0;
        }

        Integer durationMin = c.getDurationMin();
        if (durationMin == null || durationMin <= 0) {
            return 0.0;
        }

        // HR_avg — пока хардкод, в будущем из данных тренировки
        double hrAvg = DEFAULT_HR_AVG;

        // HR_threshold нужен возраст пользователя, но здесь его нет.
        // Используем дефолтный возраст 30 лет → HR_max = 190, HR_threshold = 161.5
        // Реальный расчёт будет в TssService, где есть доступ к User.
        // Здесь возвращаем "сырой" TSS без учёта возраста.
        double hrThreshold = (220 - 30) * HR_THRESHOLD_COEFFICIENT;

        return calculateTss(durationMin, hrAvg, hrThreshold);
    }

    /**
     * Расчёт TSS с учётом возраста пользователя.
     * Вызывается из TssService, где есть доступ к профилю.
     *
     * @param durationMin длительность в минутах
     * @param age         возраст пользователя
     * @return TSS
     */
    public double calculate(Integer durationMin, Integer age) {
        if (durationMin == null || durationMin <= 0) {
            return 0.0;
        }

        int effectiveAge = (age != null && age > 0 && age < 120) ? age : 30;
        double hrAvg = DEFAULT_HR_AVG;
        double hrThreshold = (220 - effectiveAge) * HR_THRESHOLD_COEFFICIENT;

        return calculateTss(durationMin, hrAvg, hrThreshold);
    }

    /**
     * Базовая формула TSS.
     * TSS = (duration_min / 60) × (HR_avg / HR_threshold) × 100
     */
    private double calculateTss(int durationMin, double hrAvg, double hrThreshold) {
        if (hrThreshold <= 0) {
            return 0.0;
        }

        double durationHours = durationMin / 60.0;
        double intensityFactor = hrAvg / hrThreshold;

        return durationHours * intensityFactor * 100.0;
    }
}
