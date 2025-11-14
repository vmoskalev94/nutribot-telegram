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

    // ===== setters для полей онбординга =====

    @Transactional
    public void updateName(Long userId, String name) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setName(name);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void updateSex(Long userId, String sex) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setSex(sex);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void updateAge(Long userId, Integer age) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setAge(age);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void updateHeightCm(Long userId, Integer heightCm) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setHeightCm(heightCm);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void updateWeightKg(Long userId, Double weightKg) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setWeightKg(weightKg);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void updateTrainingLevel(Long userId, String trainingLevel) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setTrainingLevel(trainingLevel);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void updateCity(Long userId, String city) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setCity(city);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void updateGeo(Long userId, Double lat, Double lon) {
        User user = getUserOrThrow(userId);
        user.setGeoLat(lat);
        user.setGeoLon(lon);
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void updatePhone(Long userId, String phone) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setPhone(phone);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional(readOnly = true)
    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    @Transactional
    public void updateNutrientVerbose(Long userId, boolean verbose) {
        User user = getUserOrThrow(userId);
        user.setNutrientVerbose(verbose);
        userRepository.save(user);
    }
}
