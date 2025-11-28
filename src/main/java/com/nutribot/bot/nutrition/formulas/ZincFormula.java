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
 * Формула:
 * Zn = (FFM × 0.2) + (sweat_rate × 0.1) + SS × 0.0004 + 2.5 (доп. расход)
 * <p>
 * Особенности:
 * - Только SS (для силовых), TSS не используется
 * - sweat_rate = base_sweat × 0.2, где base_sweat: М = 1.0, Ж = 0.75
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

        double value = (ffm * 0.2)
                + (sweatRate * 0.1)
                + (ss * 0.0004);

        // Дополнительный расход
        value += MvpConstants.EXTRA_ZN;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "Zn = (FFM × 0.2) + (sweat_rate × 0.1) + (SS × 0.0004) + 2.5";
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
        return vars;
    }
}
