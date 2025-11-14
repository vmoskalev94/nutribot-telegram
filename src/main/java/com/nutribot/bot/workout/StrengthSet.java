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
@Table("strength_set")
public class StrengthSet {

    @Id
    private Long id;

    @Column("exercise_id")
    private Long exerciseId;

    @Column("weight")
    private Double weight;

    @Column("reps")
    private Integer reps;

    @Column("rir")
    private Integer rir;

    @Column("order_index")
    private Integer orderIndex;
}
