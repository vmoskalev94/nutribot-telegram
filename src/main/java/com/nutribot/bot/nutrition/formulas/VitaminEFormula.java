package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин E
 * <p>
 * Формула (PDF):
 * E = clamp(15 + FFM×0.1 + pollution_level×2 + (SS+TSS)×0.00005 + EXTRA,
 *           MIN=15, MAX=1000)
 * <p>
 * Единицы: мг
 * Синергия с витамином C и селеном.
 * <p>
 * Особенности:
 * - pollution_level = 2 (MVP)
 * - Группа 2: для силовых используем SS + TSS, для кардио только TSS
 */
@Component
public class VitaminEFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "E";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double ffm = ctx.getFfmOrDefault(50.0);
        double pollutionLevel = MvpConstants.POLLUTION_LEVEL;

        // SS и TSS
        double ss = 0.0;
        double tss = ctx.getTssOrZero();
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        // Базовая формула по PDF
        double value = 15.0
                + (ffm * 0.1)
                + (pollutionLevel * 2.0)
                + ((ss + tss) * 0.00005);

        // Дополнительный расход
        value += MvpConstants.EXTRA_E;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.E_MIN, NutrientLimits.E_MAX);
    }

    @Override
    public String template() {
        return "E = clamp(15 + FFM×0.1 + pollution_level×2 + (SS+TSS)×0.00005 + extra, 15, 1000)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double ffm = ctx.getFfmOrDefault(50.0);
        double pollutionLevel = MvpConstants.POLLUTION_LEVEL;

        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;
        double tss = ctx.getTssOrZero();

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("FFM", ffm);
        vars.put("pollution_level", pollutionLevel);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("extra", MvpConstants.EXTRA_E);
        vars.put("min", NutrientLimits.E_MIN);
        vars.put("max", NutrientLimits.E_MAX);
        return vars;
    }
}
