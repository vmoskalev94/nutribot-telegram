package com.nutribot.bot.onboarding;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserOnboardingStateRepository stateRepository;

    /**
     * Начать онбординг (или перезапустить) с шага A1.
     */
    @Transactional
    public void start(Long userId) {
        UserOnboardingState state = stateRepository.findByUserId(userId)
                .orElseGet(() -> UserOnboardingState.builder()
                        .userId(userId)
                        .build());

        state.setCurrentStep(OnboardingStep.A1_GREETING);
        state.setDataJson(null);

        stateRepository.save(state);
    }

    @Transactional(readOnly = true)
    public Optional<OnboardingStep> getCurrentStep(Long userId) {
        return stateRepository.findByUserId(userId)
                .map(UserOnboardingState::getCurrentStep);
    }

    @Transactional
    public void setStep(Long userId, OnboardingStep step) {
        UserOnboardingState state = stateRepository.findByUserId(userId)
                .orElseGet(() -> UserOnboardingState.builder()
                        .userId(userId)
                        .build());
        state.setCurrentStep(step);
        stateRepository.save(state);
    }

    @Transactional
    public void reset(Long userId) {
        stateRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveOnboarding(Long userId) {
        return stateRepository.existsByUserId(userId);
    }

    @Transactional
    public void saveDataJson(Long userId, String dataJson) {
        UserOnboardingState state = stateRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Onboarding state not found for user " + userId));
        state.setDataJson(dataJson);
        stateRepository.save(state);
    }

    @Transactional(readOnly = true)
    public String getDataJson(Long userId) {
        return stateRepository.findByUserId(userId)
                .map(UserOnboardingState::getDataJson)
                .orElse(null);
    }
}
