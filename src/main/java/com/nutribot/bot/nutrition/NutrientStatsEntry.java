package com.nutribot.bot.nutrition;

import lombok.Builder;

/**
 * Агрегированная статистика по одному нутриенту за период.
 */
@Builder
public record NutrientStatsEntry(
        String code,          // код нутриента (MG, D3, ...)
        String name,          // человекочитаемое имя
        String unit,          // единица измерения (mg, IU, g)
        double totalAmount,   // суммарная рекомендованная доза за период
        double averagePerWorkout // среднее на тренировку
) {
}
