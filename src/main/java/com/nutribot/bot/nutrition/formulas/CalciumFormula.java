package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Кальций (Ca)
 * <p>
 * Формула:
 * Ca = 5 × bone_mass + (беременность ? 300 : 0) + (SS + TSS) × 10 + 15 (доп. расход)
 * <p>
 * Группа 2: для силовых используем SS + TSS, для кардио только TSS.
 * <p>
 * Особенности:
 * - bone_mass рассчитывается в BodyCompositionCalculator и передаётся в контексте
 */
@Component
public class CalciumFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "CA";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double boneMass = ctx.getBoneMassOrDefault(3.0);
        boolean isPregnant = ctx.isPregnant();

        // Группа 2: силовая → SS + TSS, кардио → только TSS
        double ss = 0.0;
        double tss = ctx.getTssOrZero();
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        double pregnancyBonus = isPregnant ? 300.0 : 0.0;

        double value = (boneMass * 5.0)
                + pregnancyBonus
                + ((ss + tss) * 10.0);

        // Дополнительный расход
        value += MvpConstants.EXTRA_CA;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "Ca = (bone_mass × 5) + pregnancy_bonus + ((SS + TSS) × 10) + 15";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double boneMass = ctx.getBoneMassOrDefault(3.0);
        boolean isPregnant = ctx.isPregnant();

        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;
        double tss = ctx.getTssOrZero();

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("bone_mass", boneMass);
        vars.put("is_pregnant", isPregnant);
        vars.put("pregnancy_bonus", isPregnant ? 300.0 : 0.0);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("extra", MvpConstants.EXTRA_CA);
        return vars;
    }
}
