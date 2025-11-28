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
 * Формула:
 * Cu = (ceruloplasmin × 0.3) + (ferritin × 0.05) + SS × 0.0003 + 0.1 (доп. расход)
 * <p>
 * Группа 2: для силовых используем SS, для кардио SS = 0.
 * <p>
 * Особенности:
 * - ceruloplasmin и ferritin выбираются по группам (пол, возраст, статус)
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

        // Группа 2: силовая → SS, кардио → SS = 0
        double ss = 0.0;
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        double value = (ceruloplasmin * 0.3)
                + (ferritin * 0.05)
                + (ss * 0.0003);

        // Дополнительный расход
        value += MvpConstants.EXTRA_CU;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "Cu = (ceruloplasmin × 0.3) + (ferritin × 0.05) + (SS × 0.0003) + 0.1";
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
        return vars;
    }
}
