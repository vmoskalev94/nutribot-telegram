package com.nutribot.bot.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

/**
 * Базовый пользователь бота.
 * Поля-слоты под онбординг и профиль — пока могут быть null.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app_user")
public class User {

    @Id
    private Long id;

    @Column("telegram_id")
    private Long telegramId;

    @Column("onboarding_completed")
    private Boolean onboardingCompleted;

    private String name;
    private String sex;
    private Integer age;

    @Column("height_cm")
    private Integer heightCm;

    @Column("weight_kg")
    private Double weightKg;

    @Column("training_level")
    private String trainingLevel;

    private String city;
    private String phone;

    @Column("geo_lat")
    private Double geoLat;

    @Column("geo_lon")
    private Double geoLon;

    @Column("nutrient_verbose")
    private Boolean nutrientVerbose;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
