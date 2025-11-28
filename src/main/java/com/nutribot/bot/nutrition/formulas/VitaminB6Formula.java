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
 * Формула:
 * B6 = 0.015 × protein_intake + SS × 0.0001 + 0.4 (доп. расход)
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
        double proteinIntake = weight * 1.5; // г/сутки

        // SS только для силовых, для кардио = 0
        double ss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        double value = (0.015 * proteinIntake)
                + (ss * 0.0001);

        // Дополнительный расход
        value += MvpConstants.EXTRA_B6;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "B6 = (protein_intake × 0.015) + (SS × 0.0001) + 0.4";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double weight = ctx.getWeightOrDefault(70.0);
        double proteinIntake = weight * 1.5;
        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("protein_intake", proteinIntake);
        vars.put("SS", ss);
        vars.put("extra", MvpConstants.EXTRA_B6);
        return vars;
    }
}
