package com.nutribot.bot.workout;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WorkoutRepository extends CrudRepository<Workout, Long> {

    @Query("""
            SELECT * FROM workout
            WHERE user_id = :userId
              AND status = :status
            ORDER BY started_at DESC
            LIMIT :limit OFFSET :offset
            """)
    List<Workout> findPageByUserAndStatusOrderByStartedAtDesc(Long userId,
                                                              WorkoutStatus status,
                                                              int limit,
                                                              long offset);
}
