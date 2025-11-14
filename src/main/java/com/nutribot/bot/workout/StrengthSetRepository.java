package com.nutribot.bot.workout;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StrengthSetRepository extends CrudRepository<StrengthSet, Long> {

    List<StrengthSet> findByExerciseIdOrderByOrderIndex(Long exerciseId);

    @Query("SELECT COALESCE(MAX(order_index), 0) FROM strength_set WHERE exercise_id = :exerciseId")
    int findMaxOrderIndexByExerciseId(Long exerciseId);
}
