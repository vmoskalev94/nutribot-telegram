package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Омега-3 (EPA + DHA)
 * <p>
 * Формула (PDF):
 * Ω3 = clamp(base_by_index + CRP×20 + (SS+TSS)×0.01 + EXTRA,
 *            MIN=250, MAX=3000)
 * <p>
 * где base_by_index:
 *   omega3_index < 4  → 2000
 *   omega3_index < 6  → 1500
 *   omega3_index < 8  → 1000
 *   omega3_index >= 8 → 500
 * <p>
 * Единицы: мг
 * Синергия с витамином E и селеном.
 * <p>
 * Особенности:
 * - Для силовых: TSS = 0
 * - Для кардио: SS = 0
 */
@Component
public class Omega3Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "OMEGA3";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double omega3Index = MvpConstants.OMEGA3_INDEX;
        double crp = MvpConstants.CRP;

        // SS и TSS в зависимости от типа тренировки
        double ss = 0.0;
        double tss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        } else if (ctx.getWorkoutType() == WorkoutType.CARDIO) {
            tss = ctx.getTssOrZero();
        }

        // Базовое значение по omega3_index (PDF)
        double baseValue;
        if (omega3Index < 4) {
            baseValue = 2000.0;
        } else if (omega3Index < 6) {
            baseValue = 1500.0;
        } else if (omega3Index < 8) {
            baseValue = 1000.0;
        } else {
            baseValue = 500.0;
        }

        // Формула по PDF
        double value = baseValue
                + (crp * 20.0)
                + ((ss + tss) * 0.01);

        // Дополнительный расход
        value += MvpConstants.EXTRA_OMEGA3;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.OMEGA3_MIN, NutrientLimits.OMEGA3_MAX);
    }

    @Override
    public String template() {
        return "Ω3 = clamp(base_by_index + CRP×20 + (SS+TSS)×0.01 + extra, 250, 3000)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double omega3Index = MvpConstants.OMEGA3_INDEX;
        double crp = MvpConstants.CRP;
        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;
        double tss = ctx.getWorkoutType() == WorkoutType.CARDIO ? ctx.getTssOrZero() : 0.0;

        double baseValue;
        if (omega3Index < 4) {
            baseValue = 2000.0;
        } else if (omega3Index < 6) {
            baseValue = 1500.0;
        } else if (omega3Index < 8) {
            baseValue = 1000.0;
        } else {
            baseValue = 500.0;
        }

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("omega3_index", omega3Index);
        vars.put("base_by_index", baseValue);
        vars.put("CRP", crp);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("extra", MvpConstants.EXTRA_OMEGA3);
        vars.put("min", NutrientLimits.OMEGA3_MIN);
        vars.put("max", NutrientLimits.OMEGA3_MAX);
        return vars;
    }
}
