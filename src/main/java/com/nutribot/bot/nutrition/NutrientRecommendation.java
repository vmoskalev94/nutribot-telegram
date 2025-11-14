package com.nutribot.bot.nutrition;

import lombok.Builder;

/**
 * Рекомендация по конкретному нутриенту.
 * <p>
 * code  – код нутриента (например, "MG", "D3", "PROTEIN"),
 * name  – человекочитаемое имя,
 * unit  – единица измерения ("mg", "IU", "g" и т.п.),
 * value – рекомендованное количество.
 */
@Builder
public record NutrientRecommendation(
        String code,
        String name,
        String unit,
        double value
) {
}
