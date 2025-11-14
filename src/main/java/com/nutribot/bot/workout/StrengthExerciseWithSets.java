package com.nutribot.bot.workout;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class StrengthExerciseWithSets {
    StrengthExercise exercise;
    List<StrengthSet> sets;
}
