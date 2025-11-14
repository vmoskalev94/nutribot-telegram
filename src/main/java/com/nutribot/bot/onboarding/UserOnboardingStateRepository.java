package com.nutribot.bot.onboarding;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserOnboardingStateRepository extends CrudRepository<UserOnboardingState, Long> {

    Optional<UserOnboardingState> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);
}
