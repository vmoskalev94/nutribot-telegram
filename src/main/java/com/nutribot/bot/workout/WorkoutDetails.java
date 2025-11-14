package com.nutribot.bot.workout;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Сводка по тренировке:
 * - для силовой: workout + список упражнений с подходами;
 * - для кардио: workout + cardioDetails.
 */
@Value
@Builder
public class WorkoutDetails {

    Workout workout;
    List<StrengthExerciseWithSets> strengthExercises; // для STRENGTH, иначе null/пусто
    CardioWorkoutDetails cardioDetails;               // для CARDIO, иначе null
}
