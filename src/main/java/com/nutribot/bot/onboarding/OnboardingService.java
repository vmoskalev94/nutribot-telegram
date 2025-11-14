package com.nutribot.bot.onboarding;

import com.nutribot.bot.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserOnboardingStateRepository stateRepository;
    private final UserService userService;

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
        log.info("Onboarding started for userId={}", userId);
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
        log.debug("Onboarding step updated: userId={}, step={}", userId, step);
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

    @Transactional
    public void complete(Long userId) {
        userService.markOnboardingCompleted(userId);
        stateRepository.deleteByUserId(userId);
        log.info("Onboarding completed for userId={}", userId);
    }
}
