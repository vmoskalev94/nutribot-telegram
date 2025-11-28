package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Йод (I)
 * <p>
 * Формула:
 * I = (thyroid_volume × 0.4) + (беременность ? 150 : 0) + (SS > 20000 ? 40 : 0) + 15 (доп. расход)
 * <p>
 * Группа 2: для силовых используем SS, для кардио SS = 0.
 * <p>
 * Особенности:
 * - thyroid_volume = 1 мл (MVP)
 * - SS > 20000 — высокоинтенсивные силовые тренировки
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

        // Группа 2: силовая → SS, кардио → SS = 0
        double ss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        // Модификаторы
        double pregnancyBonus = isPregnant ? 150.0 : 0.0;
        double highIntensityBonus = (ss > 20000) ? 40.0 : 0.0;

        double value = (thyroidVolume * 0.4)
                + pregnancyBonus
                + highIntensityBonus;

        // Дополнительный расход
        value += MvpConstants.EXTRA_I;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "I = (thyroid_volume × 0.4) + pregnancy_bonus + high_intensity_bonus + 15";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double thyroidVolume = MvpConstants.THYROID_VOLUME;
        boolean isPregnant = ctx.isPregnant();

        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("thyroid_volume", thyroidVolume);
        vars.put("is_pregnant", isPregnant);
        vars.put("pregnancy_bonus", isPregnant ? 150.0 : 0.0);
        vars.put("SS", ss);
        vars.put("high_intensity_bonus", ss > 20000 ? 40.0 : 0.0);
        vars.put("extra", MvpConstants.EXTRA_I);
        return vars;
    }
}
