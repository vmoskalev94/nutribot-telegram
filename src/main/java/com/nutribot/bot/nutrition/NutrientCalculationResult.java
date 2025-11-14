package com.nutribot.bot.nutrition;

import lombok.Builder;

import java.util.List;

/**
 * Результат расчёта нутриентов:
 * - использованный контекст;
 * - список рекомендаций.
 */
@Builder
public record NutrientCalculationResult(
        NutrientContext context,
        List<NutrientRecommendation> recommendations
) {
}
