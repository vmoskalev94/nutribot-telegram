package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин B12
 * <p>
 * Формула:
 * B12 = (homocysteine > 12 ? 5.0 : 2.4) + (vegan_years × 0.5) + (SS + TSS) × 0.1 + 0.3 (доп. расход)
 * <p>
 * Группа 2: для силовых используем SS + TSS, для кардио только TSS.
 * <p>
 * Особенности:
 * - homocysteine = 8 мкмоль/л (MVP)
 * - vegan_years = 0 для не-веганов
 */
@Component
public class VitaminB12Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "B12";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double homocysteine = MvpConstants.HOMOCYSTEINE;
        double veganYears = ctx.isVegan() ? 1.0 : MvpConstants.VEGAN_YEARS_DEFAULT;

        // Группа 2: силовая → SS + TSS, кардио → только TSS
        double ss = 0.0;
        double tss = ctx.getTssOrZero();
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        // Базовое значение по гомоцистеину
        double baseValue = (homocysteine > 12) ? 5.0 : 2.4;

        double value = baseValue
                + (veganYears * 0.5)
                + ((ss + tss) * 0.1);

        // Дополнительный расход
        value += MvpConstants.EXTRA_B12;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "B12 = homocysteine_bonus + (vegan_years × 0.5) + ((SS + TSS) × 0.1) + 0.3";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double homocysteine = MvpConstants.HOMOCYSTEINE;
        double veganYears = ctx.isVegan() ? 1.0 : MvpConstants.VEGAN_YEARS_DEFAULT;
        double baseValue = (homocysteine > 12) ? 5.0 : 2.4;

        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;
        double tss = ctx.getTssOrZero();

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("homocysteine", homocysteine);
        vars.put("homocysteine_bonus", baseValue);
        vars.put("vegan_years", veganYears);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("extra", MvpConstants.EXTRA_B12);
        return vars;
    }
}
