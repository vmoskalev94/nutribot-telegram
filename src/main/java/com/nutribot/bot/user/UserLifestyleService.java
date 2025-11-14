package com.nutribot.bot.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserLifestyleService {

    private final UserLifestyleRepository repository;

    @Transactional(readOnly = true)
    public Optional<UserLifestyle> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Transactional
    public UserLifestyle getOrCreate(Long userId) {
        return repository.findByUserId(userId)
                .orElseGet(() -> repository.save(
                        UserLifestyle.builder()
                                .userId(userId)
                                .build()
                ));
    }

    @Transactional
    public void updateSmoking(Long userId, Double packsPerDay) {
        UserLifestyle lifestyle = getOrCreate(userId);
        lifestyle.setSmokePacksPerDay(packsPerDay);
        repository.save(lifestyle);
    }

    @Transactional
    public void updateVegan(Long userId, boolean vegan) {
        UserLifestyle lifestyle = getOrCreate(userId);
        lifestyle.setVegan(vegan);
        repository.save(lifestyle);
    }

    @Transactional
    public void updatePregnant(Long userId, boolean pregnant) {
        UserLifestyle lifestyle = getOrCreate(userId);
        lifestyle.setPregnant(pregnant);
        repository.save(lifestyle);
    }
}
