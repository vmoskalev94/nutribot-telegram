package com.nutribot.bot.workout.tss;

import com.nutribot.bot.workout.WorkoutDetails;
import com.nutribot.bot.workout.WorkoutType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TssService {

    private final List<TssCalculator> calculators;

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
}
