package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Фолат (B9)
 * <p>
 * Формула (PDF):
 * B9 = clamp(400 + (mthfr_mutation ? 200 : 0) + SS×0.00005 + EXTRA,
 *            MIN=400, MAX=1000)
 * <p>
 * Единицы: мкг
 * Синергия с B12. Дефицит — гипергомоцистеинемия.
 * <p>
 * Особенности:
 * - mthfr_mutation = false (MVP)
 * - Группа 2: для силовых используем SS, для кардио SS = 0
 */
@Component
public class FolateB9Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "B9";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        boolean mthfrMutation = MvpConstants.MTHFR_MUTATION;

        // SS только для силовых, для кардио = 0
        double ss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        // Базовая формула по PDF
        double value = 400.0;

        // Модификатор MTHFR
        if (mthfrMutation) {
            value += 200.0;
        }
        value += ss * 0.00005;

        // Дополнительный расход
        value += MvpConstants.EXTRA_B9;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.B9_MIN, NutrientLimits.B9_MAX);
    }

    @Override
    public String template() {
        return "B9 = clamp(400 + mthfr_bonus + SS×0.00005 + extra, 400, 1000)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        boolean mthfrMutation = MvpConstants.MTHFR_MUTATION;
        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("mthfr_mutation", mthfrMutation);
        vars.put("mthfr_bonus", mthfrMutation ? 200.0 : 0.0);
        vars.put("SS", ss);
        vars.put("extra", MvpConstants.EXTRA_B9);
        vars.put("min", NutrientLimits.B9_MIN);
        vars.put("max", NutrientLimits.B9_MAX);
        return vars;
    }
}
