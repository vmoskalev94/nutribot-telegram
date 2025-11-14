package com.nutribot.bot.workout;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CardioWorkoutDetailsRepository extends CrudRepository<CardioWorkoutDetails, Long> {

    Optional<CardioWorkoutDetails> findByWorkoutId(Long workoutId);
}
