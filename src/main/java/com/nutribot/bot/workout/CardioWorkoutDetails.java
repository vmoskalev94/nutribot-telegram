package com.nutribot.bot.workout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cardio_workout_details")
public class CardioWorkoutDetails {

    @Id
    private Long id;

    @Column("workout_id")
    private Long workoutId;

    @Column("activity_type")
    private String activityType;

    @Column("duration_min")
    private Integer durationMin;

    @Column("distance_km")
    private Double distanceKm;

    @Column("intensity")
    private String intensity;  // LOW / MODERATE / HIGH

    @Column("rpe")
    private Integer rpe;       // 1–10
}
