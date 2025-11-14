package com.nutribot.bot.workout.tss;

import com.nutribot.bot.workout.CardioWorkoutDetails;
import com.nutribot.bot.workout.WorkoutDetails;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

@Component
public class CardioTssCalculator implements TssCalculator {

    @Override
    public boolean supports(WorkoutType type) {
        return type == WorkoutType.CARDIO;
    }

    @Override
    public double calculate(WorkoutDetails details) {
        CardioWorkoutDetails c = details.getCardioDetails();
        if (c == null) {
            return 0.0; // дефолтное значение TSS для кардио тренировок без деталей
        }

        // TODO: сюда потом ставишь формулу из дока для кардио.
        // пример:
        // double duration = Optional.ofNullable(c.getDurationMin()).orElse(0);
        // double rpe = Optional.ofNullable(c.getRpe()).orElse(0);
        // return ...;
        return 0.0;
    }
}
