package com.nutribot.bot.workout;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
    public class CardioCreationStateStore {

    private final Map<Long, CardioCreationState> states = new ConcurrentHashMap<>();

    public void setState(Long userId, CardioCreationState state) {
        states.put(userId, state);
    }

    public Optional<CardioCreationState> getState(Long userId) {
        return Optional.ofNullable(states.get(userId));
    }

    public void clear(Long userId) {
        states.remove(userId);
    }
}
