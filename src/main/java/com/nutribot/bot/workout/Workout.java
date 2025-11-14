package com.nutribot.bot.workout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("workout")
public class Workout {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("type")
    private WorkoutType type;

    @Column("started_at")
    private OffsetDateTime startedAt;

    @Column("status")
    private WorkoutStatus status;
}
