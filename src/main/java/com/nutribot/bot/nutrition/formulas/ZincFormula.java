package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Цинк (Zn)
 * <p>
 * Формула (PDF):
 * Zn = clamp(10 + FFM×0.1 + sweat_rate×0.05 + SS×0.0002 + EXTRA,
 *            MIN=8, MAX=30)
 * <p>
 * Единицы: мг
 * Антагонизм с Fe. sweat_rate = base_sweat × 0.2
 * <p>
 * Особенности:
 * - Только SS (для силовых), TSS не используется
 */
@Component
public class ZincFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "ZN";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double ffm = ctx.getFfmOrDefault(50.0);
        double sweatRate = ctx.getSweatRateOrDefault(0.2);

        // SS только для силовых, для кардио = 0
        double ss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        // Базовая формула по PDF
        double value = 10.0
                + (ffm * 0.1)
                + (sweatRate * 0.05)
                + (ss * 0.0002);

        // Дополнительный расход
        value += MvpConstants.EXTRA_ZN;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.ZN_MIN, NutrientLimits.ZN_MAX);
    }

    @Override
    public String template() {
        return "Zn = clamp(10 + FFM×0.1 + sweat_rate×0.05 + SS×0.0002 + extra, 8, 30)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double ffm = ctx.getFfmOrDefault(50.0);
        double sweatRate = ctx.getSweatRateOrDefault(0.2);
        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("FFM", ffm);
        vars.put("sweat_rate", sweatRate);
        vars.put("SS", ss);
        vars.put("extra", MvpConstants.EXTRA_ZN);
        vars.put("min", NutrientLimits.ZN_MIN);
        vars.put("max", NutrientLimits.ZN_MAX);
        return vars;
    }
}
