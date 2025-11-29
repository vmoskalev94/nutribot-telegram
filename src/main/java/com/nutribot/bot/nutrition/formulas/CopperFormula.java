package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Медь (Cu)
 * <p>
 * Формула (PDF):
 * Cu = clamp(0.9 + ceruloplasmin×0.01 + ferritin×0.001 + SS×0.00001 + EXTRA,
 *            MIN=0.9, MAX=10)
 * <p>
 * Единицы: мг
 * Антагонизм с цинком. Дефицит — анемия, атаксия.
 * <p>
 * Особенности:
 * - ceruloplasmin и ferritin выбираются по группам (пол, возраст, статус)
 * - Группа 2: для силовых используем SS, для кардио SS = 0
 */
@Component
public class CopperFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "CU";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        boolean isMale = ctx.isMale();
        boolean isPregnant = ctx.isPregnant();
        boolean isAthlete = ctx.isAthlete();
        boolean isVegan = ctx.isVegan();
        Integer age = ctx.getAgeYears();

        double ceruloplasmin = MvpConstants.getCeruloplasmin(isMale, isPregnant, isAthlete, isVegan);
        double ferritin = MvpConstants.getFerritin(isMale, age, isPregnant, isVegan);

        // SS только для силовых, для кардио = 0
        double ss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        // Базовая формула по PDF
        double value = 0.9
                + (ceruloplasmin * 0.01)
                + (ferritin * 0.001)
                + (ss * 0.00001);

        // Дополнительный расход
        value += MvpConstants.EXTRA_CU;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.CU_MIN, NutrientLimits.CU_MAX);
    }

    @Override
    public String template() {
        return "Cu = clamp(0.9 + ceruloplasmin×0.01 + ferritin×0.001 + SS×0.00001 + extra, 0.9, 10)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        boolean isMale = ctx.isMale();
        boolean isPregnant = ctx.isPregnant();
        boolean isAthlete = ctx.isAthlete();
        boolean isVegan = ctx.isVegan();
        Integer age = ctx.getAgeYears();

        double ceruloplasmin = MvpConstants.getCeruloplasmin(isMale, isPregnant, isAthlete, isVegan);
        double ferritin = MvpConstants.getFerritin(isMale, age, isPregnant, isVegan);
        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("ceruloplasmin", ceruloplasmin);
        vars.put("ferritin", ferritin);
        vars.put("SS", ss);
        vars.put("extra", MvpConstants.EXTRA_CU);
        vars.put("min", NutrientLimits.CU_MIN);
        vars.put("max", NutrientLimits.CU_MAX);
        return vars;
    }
}
