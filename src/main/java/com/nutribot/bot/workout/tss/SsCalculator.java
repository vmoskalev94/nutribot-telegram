package com.nutribot.bot.workout.tss;

import com.nutribot.bot.workout.StrengthExerciseWithSets;
import com.nutribot.bot.workout.StrengthSet;
import com.nutribot.bot.workout.WorkoutDetails;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Калькулятор Strength Score (SS) для силовых тренировок.
 * <p>
 * Формула:
 * SS = Σ (sets × reps × weight) × (RPE / 10) × (1 + log₁₀(1 + RIR))
 * <p>
 * Где RPE = 10 - RIR (выводим из RIR).
 * <p>
 * SS используется для нутриентов, связанных с нейромышечной функцией:
 * Mg, Zn, B6, Омега-3, Vit C.
 */
@Component
public class SsCalculator {

    /**
     * Рассчитывает суммарный SS по всем упражнениям и подходам тренировки.
     *
     * @param details детали тренировки
     * @return суммарный SS (0 для кардио или если нет подходов)
     */
    public double calculate(WorkoutDetails details) {
        if (details == null) {
            return 0.0;
        }

        List<StrengthExerciseWithSets> exercises = details.getStrengthExercises();
        if (exercises == null || exercises.isEmpty()) {
            return 0.0;
        }

        double totalSs = 0.0;

        for (StrengthExerciseWithSets exerciseBlock : exercises) {
            List<StrengthSet> sets = exerciseBlock.getSets();
            if (sets == null) {
                continue;
            }

            for (StrengthSet set : sets) {
                totalSs += calculateSetSs(set);
            }
        }

        return totalSs;
    }

    /**
     * Рассчитывает SS для одного подхода.
     * <p>
     * SS_set = (1 × reps × weight) × (RPE / 10) × (1 + log₁₀(1 + RIR))
     * <p>
     * Примечание: "sets" в формуле = 1 для каждого отдельного подхода,
     * суммирование происходит на уровне всей тренировки.
     */
    private double calculateSetSs(StrengthSet set) {
        if (set == null) {
            return 0.0;
        }

        Double weight = set.getWeight();
        Integer reps = set.getReps();
        Integer rir = set.getRir();

        // Защита от null и невалидных значений
        if (weight == null || weight <= 0) {
            return 0.0;
        }
        if (reps == null || reps <= 0) {
            return 0.0;
        }

        // RIR по умолчанию = 2 (умеренно тяжёлый подход)
        int effectiveRir = (rir != null && rir >= 0 && rir <= 10) ? rir : 2;

        // RPE = 10 - RIR
        int rpe = 10 - effectiveRir;

        // Защита: RPE должен быть в диапазоне 1-10
        rpe = Math.max(1, Math.min(10, rpe));

        // SS = (1 × reps × weight) × (RPE / 10) × (1 + log₁₀(1 + RIR))
        double volumeComponent = reps * weight;
        double rpeComponent = rpe / 10.0;
        double rirComponent = 1 + Math.log10(1 + effectiveRir);

        return volumeComponent * rpeComponent * rirComponent;
    }
}
