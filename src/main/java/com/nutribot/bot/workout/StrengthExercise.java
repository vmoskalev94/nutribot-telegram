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
@Table("strength_exercise")
public class StrengthExercise {

    @Id
    private Long id;

    @Column("workout_id")
    private Long workoutId;

    @Column("name")
    private String name;

    @Column("order_index")
    private Integer orderIndex;
}
