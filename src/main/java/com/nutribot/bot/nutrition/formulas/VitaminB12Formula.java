package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин B12
 * <p>
 * Формула (PDF):
 * B12 = clamp(2.4 + (homocysteine > 10 ? 2.0 : 0) + (vegan_years > 0 ? 2.0 : 0) + EXTRA,
 *             MIN=2.4, MAX=1000)
 * <p>
 * Единицы: мкг
 * Безопасен даже при высоких дозах. Синергия с B6 и B9.
 */
@Component
public class VitaminB12Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "B12";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double homocysteine = MvpConstants.HOMOCYSTEINE;
        double veganYears = ctx.isVegan() ? 1.0 : MvpConstants.VEGAN_YEARS_DEFAULT;

        // Базовая формула по PDF
        double value = 2.4;

        // Модификаторы
        if (homocysteine > 10) {
            value += 2.0;
        }
        if (veganYears > 0) {
            value += 2.0;
        }

        // Дополнительный расход
        value += MvpConstants.EXTRA_B12;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.B12_MIN, NutrientLimits.B12_MAX);
    }

    @Override
    public String template() {
        return "B12 = clamp(2.4 + homocysteine_bonus + vegan_bonus + extra, 2.4, 1000)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double homocysteine = MvpConstants.HOMOCYSTEINE;
        double veganYears = ctx.isVegan() ? 1.0 : MvpConstants.VEGAN_YEARS_DEFAULT;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("homocysteine", homocysteine);
        vars.put("homocysteine_bonus", homocysteine > 10 ? 2.0 : 0.0);
        vars.put("vegan_years", veganYears);
        vars.put("vegan_bonus", veganYears > 0 ? 2.0 : 0.0);
        vars.put("extra", MvpConstants.EXTRA_B12);
        vars.put("min", NutrientLimits.B12_MIN);
        vars.put("max", NutrientLimits.B12_MAX);
        return vars;
    }
}
