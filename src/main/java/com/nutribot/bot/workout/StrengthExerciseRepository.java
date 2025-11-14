package com.nutribot.bot.workout;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StrengthExerciseRepository extends CrudRepository<StrengthExercise, Long> {

    List<StrengthExercise> findByWorkoutIdOrderByOrderIndex(Long workoutId);

    @Query("SELECT COALESCE(MAX(order_index), 0) FROM strength_exercise WHERE workout_id = :workoutId")
    int findMaxOrderIndexByWorkoutId(Long workoutId);
}
