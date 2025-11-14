package com.nutribot.bot.workout;

import lombok.Builder;
import lombok.Value;

/**
 * Состояние создания тренировки.
 * Для силовой:
 * - workoutId — текущая тренировка,
 * - currentExerciseId — текущее упражнение (null, если ждём название).
 */
@Value
@Builder
public class WorkoutCreationState {
    Long workoutId;
    WorkoutType type;
    Long currentExerciseId;
}
