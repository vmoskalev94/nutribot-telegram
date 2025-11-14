package com.nutribot.bot.workout;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory хранилище состояния "юзер сейчас создаёт тренировку".
 * Ключ = userId.
 */
@Component
public class WorkoutCreationStateStore {

    private final Map<Long, WorkoutCreationState> states = new ConcurrentHashMap<>();

    public void setState(Long userId, WorkoutCreationState state) {
        states.put(userId, state);
    }

    public Optional<WorkoutCreationState> getState(Long userId) {
        return Optional.ofNullable(states.get(userId));
    }

    public void clear(Long userId) {
        states.remove(userId);
    }
}
