package com.nutribot.bot.onboarding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Текущее состояние онбординга для пользователя.
 * PK = id, userId — просто ссылка на app_user.id (уникальная).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_onboarding_state")
public class UserOnboardingState {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("current_step")
    private OnboardingStep currentStep;

    @Column("data_json")
    private String dataJson;
}
