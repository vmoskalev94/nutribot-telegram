package com.nutribot.bot.profile;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory состояние "какое поле профиля сейчас редактирует пользователь".
 * Ключ = userId.
 */
@Component
public class ProfileEditStateStore {

    private final Map<Long, ProfileEditField> state = new ConcurrentHashMap<>();

    public void setField(Long userId, ProfileEditField field) {
        state.put(userId, field);
    }

    public Optional<ProfileEditField> getField(Long userId) {
        return Optional.ofNullable(state.get(userId));
    }

    public void clear(Long userId) {
        state.remove(userId);
    }
}
