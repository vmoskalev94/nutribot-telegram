package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин B6
 * <p>
 * Формула (PDF):
 * B6 = clamp(1.5 + protein_intake×0.01 + SS×0.00005 + EXTRA,
 *            MIN=1.3, MAX=20)
 * <p>
 * Единицы: мг
 * Токсичен при >100 мг/сут. Синергия с B12 и фолатом.
 * <p>
 * Особенности:
 * - protein_intake = вес × 1.5 г
 * - Только SS (для силовых), TSS не используется
 */
@Component
public class VitaminB6Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "B6";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double weight = ctx.getWeightOrDefault(70.0);
        double proteinIntake = MvpConstants.getProteinIntake(weight);

        // SS только для силовых, для кардио = 0
        double ss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        // Базовая формула по PDF
        double value = 1.5
                + (proteinIntake * 0.01)
                + (ss * 0.00005);

        // Дополнительный расход
        value += MvpConstants.EXTRA_B6;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.B6_MIN, NutrientLimits.B6_MAX);
    }

    @Override
    public String template() {
        return "B6 = clamp(1.5 + protein_intake×0.01 + SS×0.00005 + extra, 1.3, 20)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double weight = ctx.getWeightOrDefault(70.0);
        double proteinIntake = MvpConstants.getProteinIntake(weight);
        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("weight", weight);
        vars.put("protein_intake", proteinIntake);
        vars.put("SS", ss);
        vars.put("extra", MvpConstants.EXTRA_B6);
        vars.put("min", NutrientLimits.B6_MIN);
        vars.put("max", NutrientLimits.B6_MAX);
        return vars;
    }
}
