package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин C
 * <p>
 * Формула (PDF):
 * C = clamp(90 + weight×0.5 + AQI×0.2 + (SS+TSS)×0.002 + EXTRA,
 *           MIN=90, MAX=2000)
 * <p>
 * Единицы: мг
 * Улучшает усвоение Fe. Антагонизм: оксалатурия при избытке.
 * <p>
 * Особенности:
 * - Для силовых: TSS = 0
 * - Для кардио: SS = 0
 */
@Component
public class VitaminCFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "C";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double weight = ctx.getWeightOrDefault(70.0);
        double aqi = MvpConstants.AQI;

        // SS и TSS в зависимости от типа тренировки
        double ss = 0.0;
        double tss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        } else if (ctx.getWorkoutType() == WorkoutType.CARDIO) {
            tss = ctx.getTssOrZero();
        }

        // Базовая формула по PDF
        double value = 90.0
                + (weight * 0.5)
                + (aqi * 0.2)
                + ((ss + tss) * 0.002);

        // Дополнительный расход
        value += MvpConstants.EXTRA_C;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.C_MIN, NutrientLimits.C_MAX);
    }

    @Override
    public String template() {
        return "C = clamp(90 + weight×0.5 + AQI×0.2 + (SS+TSS)×0.002 + extra, 90, 2000)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double weight = ctx.getWeightOrDefault(70.0);
        double aqi = MvpConstants.AQI;
        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;
        double tss = ctx.getWorkoutType() == WorkoutType.CARDIO ? ctx.getTssOrZero() : 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("weight", weight);
        vars.put("AQI", aqi);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("extra", MvpConstants.EXTRA_C);
        vars.put("min", NutrientLimits.C_MIN);
        vars.put("max", NutrientLimits.C_MAX);
        return vars;
    }
}
