package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин K
 * <p>
 * Формула:
 * K = (bone_mass × 0.05) + SS × 0.0001 + (warfarin ? 100 : 0) + 20 (доп. расход)
 * <p>
 * Группа 2: для силовых используем SS, для кардио SS = 0.
 * <p>
 * Особенности:
 * - bone_mass из контекста
 * - warfarin = false (MVP)
 */
@Component
public class VitaminKFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "K";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double boneMass = ctx.getBoneMassOrDefault(3.0);
        boolean warfarin = MvpConstants.WARFARIN;

        // Группа 2: силовая → SS, кардио → SS = 0
        double ss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        double warfarinBonus = warfarin ? 100.0 : 0.0;

        double value = (boneMass * 0.05)
                + (ss * 0.0001)
                + warfarinBonus;

        // Дополнительный расход
        value += MvpConstants.EXTRA_K;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "K = (bone_mass × 0.05) + (SS × 0.0001) + warfarin_bonus + 20";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double boneMass = ctx.getBoneMassOrDefault(3.0);
        boolean warfarin = MvpConstants.WARFARIN;

        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("bone_mass", boneMass);
        vars.put("SS", ss);
        vars.put("warfarin", warfarin);
        vars.put("warfarin_bonus", warfarin ? 100.0 : 0.0);
        vars.put("extra", MvpConstants.EXTRA_K);
        return vars;
    }
}
