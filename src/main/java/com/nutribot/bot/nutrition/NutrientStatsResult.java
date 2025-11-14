package com.nutribot.bot.nutrition;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * Результат статистики по нутриентам за период.
 */
@Builder
public record NutrientStatsResult(
        LocalDate fromDate,
        LocalDate toDate,
        int workoutsCount,
        List<NutrientStatsEntry> entries
) {
}
