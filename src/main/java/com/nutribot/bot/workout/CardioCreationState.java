package com.nutribot.bot.workout;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CardioCreationState {
    CardioCreationStep step;
    String activityType;
    Integer durationMin;
    Double distanceKm;
    String intensity;  // LOW / MODERATE / HIGH
    Integer rpe;       // 1–10, если вводили RPE
}
