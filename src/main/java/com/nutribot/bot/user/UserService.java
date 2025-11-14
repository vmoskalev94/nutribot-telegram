package com.nutribot.bot.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public Long ensureUserByTelegramId(Long telegramUserId) {
        return userRepository.findByTelegramId(telegramUserId)
                .map(User::getId)
                .orElseGet(() -> createUserIfNotExists(telegramUserId));
    }

    private Long createUserIfNotExists(Long telegramUserId) {
        try {
            OffsetDateTime now = OffsetDateTime.now();

            User user = User.builder()
                    .telegramId(telegramUserId)
                    .onboardingCompleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            User saved = userRepository.save(user);
            return saved.getId();
        } catch (DataIntegrityViolationException e) {
            // кто-то создал запись параллельно — читаем ещё раз
            return userRepository.findByTelegramId(telegramUserId)
                    .map(User::getId)
                    .orElseThrow(() -> e);
        }
    }

    @Transactional(readOnly = true)
    public boolean isOnboardingCompleted(Long userId) {
        return userRepository.findById(userId)
                .map(User::getOnboardingCompleted)
                .orElse(false);
    }

    @Transactional
    public void markOnboardingCompleted(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setOnboardingCompleted(true);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);
        });
    }
}
