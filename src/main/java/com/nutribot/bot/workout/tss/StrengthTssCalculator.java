package com.nutribot.bot.workout.tss;

import com.nutribot.bot.workout.WorkoutDetails;
import com.nutribot.bot.workout.WorkoutType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StrengthTssCalculator implements TssCalculator {

    @Override
    public boolean supports(WorkoutType type) {
        return type == WorkoutType.STRENGTH;
    }

    @Override
    public double calculate(WorkoutDetails details) {
        // предположим, что в WorkoutDetails есть что-то вроде:
        // List<StrengthExerciseDetails> getStrengthExercises()
        // а внутри каждого упражнения List<StrengthSetDetails> getSets()
        if (details.getStrengthExercises() == null) {
            return 0.0;
        }

        double total = 0.0;

        for (var exercise : details.getStrengthExercises()) {
            if (exercise.getSets() == null) {
                continue;
            }

            for (var set : exercise.getSets()) {
                int rir = set.getRir() == null ? 4 : set.getRir(); //todo обязательно нужен RIR, но в тренировках RIR опционален - подумать

                double rpe = (10.0 - rir); // используем только RIR для расчета RPE

                // safety-клиппинги
                if (rpe < 0) rpe = 0;
                if (rpe > 10) rpe = 10;

                int rirClamped = Math.min(Math.max(rir, 0), 5); // [0;5]

                double tssSet = (rpe / 10.0) * (1.0 + Math.pow(5 - rirClamped, 1.5));
                total += tssSet;
            }
        }

        return total;
    }
}
