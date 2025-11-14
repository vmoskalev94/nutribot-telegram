package com.nutribot.bot.workout.tss;

import com.nutribot.bot.workout.WorkoutDetails;
import com.nutribot.bot.workout.WorkoutType;

public interface TssCalculator {

    /**
     * Поддерживает ли этот калькулятор данный тип тренировки.
     */
    boolean supports(WorkoutType type);

    /**
     * Считает общий TSS по тренировке.
     */
    double calculate(WorkoutDetails details);
}
