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
 * Формула:
 * E = (FFM × 0.8) + (pollution_level × 20) + (SS + TSS) × 0.0008 + 3 (доп. расход)
 * <p>
 * Группа 2: для силовых используем SS + TSS, для кардио только TSS.
 * <p>
 * Особенности:
 * - pollution_level = 2 (MVP)
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

        // Группа 2: силовая → SS + TSS, кардио → только TSS
        double ss = 0.0;
        double tss = ctx.getTssOrZero();
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        double value = (ffm * 0.8)
                + (pollutionLevel * 20.0)
                + ((ss + tss) * 0.0008);

        // Дополнительный расход
        value += MvpConstants.EXTRA_E;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "E = (FFM × 0.8) + (pollution_level × 20) + ((SS + TSS) × 0.0008) + 3";
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
        return vars;
    }
}
