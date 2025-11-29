package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Йод (I)
 * <p>
 * Формула (PDF):
 * I = clamp(150 + thyroid_volume×2 + (pregnancy ? 100 : 0) + EXTRA,
 *           MIN=150, MAX=1100)
 * <p>
 * Единицы: мкг
 * Синергия с селеном. Избыток — риск тиреоидита.
 * <p>
 * Особенности:
 * - thyroid_volume = 1 мл (MVP)
 */
@Component
public class IodineFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "I";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double thyroidVolume = MvpConstants.THYROID_VOLUME;
        boolean isPregnant = ctx.isPregnant();

        // Базовая формула по PDF
        double value = 150.0
                + (thyroidVolume * 2.0);

        // Модификатор беременности
        if (isPregnant) {
            value += 100.0;
        }

        // Дополнительный расход
        value += MvpConstants.EXTRA_I;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.I_MIN, NutrientLimits.I_MAX);
    }

    @Override
    public String template() {
        return "I = clamp(150 + thyroid_volume×2 + pregnancy_bonus + extra, 150, 1100)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double thyroidVolume = MvpConstants.THYROID_VOLUME;
        boolean isPregnant = ctx.isPregnant();

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("thyroid_volume", thyroidVolume);
        vars.put("is_pregnant", isPregnant);
        vars.put("pregnancy_bonus", isPregnant ? 100.0 : 0.0);
        vars.put("extra", MvpConstants.EXTRA_I);
        vars.put("min", NutrientLimits.I_MIN);
        vars.put("max", NutrientLimits.I_MAX);
        return vars;
    }
}
