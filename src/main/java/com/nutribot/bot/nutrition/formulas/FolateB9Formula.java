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
 * Формула:
 * B9 = (FFM × 0.8) + (mthfr_mutation ? 400 : 0) + SS × 0.00015 + 40 (доп. расход)
 * <p>
 * Группа 2: для силовых используем SS, для кардио SS = 0.
 * <p>
 * Особенности:
 * - mthfr_mutation = false (MVP)
 */
@Component
public class FolateB9Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "B9";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double ffm = ctx.getFfmOrDefault(50.0);
        boolean mthfrMutation = MvpConstants.MTHFR_MUTATION;

        // Группа 2: силовая → SS, кардио → SS = 0
        double ss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        double mthfrBonus = mthfrMutation ? 400.0 : 0.0;

        double value = (ffm * 0.8)
                + mthfrBonus
                + (ss * 0.00015);

        // Дополнительный расход
        value += MvpConstants.EXTRA_B9;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "B9 = (FFM × 0.8) + mthfr_bonus + (SS × 0.00015) + 40";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double ffm = ctx.getFfmOrDefault(50.0);
        boolean mthfrMutation = MvpConstants.MTHFR_MUTATION;

        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("FFM", ffm);
        vars.put("mthfr_mutation", mthfrMutation);
        vars.put("mthfr_bonus", mthfrMutation ? 400.0 : 0.0);
        vars.put("SS", ss);
        vars.put("extra", MvpConstants.EXTRA_B9);
        return vars;
    }
}
