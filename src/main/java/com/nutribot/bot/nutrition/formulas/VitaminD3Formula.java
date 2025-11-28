package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import com.nutribot.bot.workout.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин D3
 * <p>
 * Формула:
 * D3 = (вес × 20) + (возраст > 50 ? 1000 : 0) + (latitude > 40 ? 2000 : 0) + (SS + TSS) × 10 + 200 (доп. расход)
 * <p>
 * Группа 2: для силовых используем SS + TSS, для кардио только TSS.
 * <p>
 * Особенности:
 * - latitude = 55.75 (Москва, >40 → низкая инсоляция)
 */
@Component
public class VitaminD3Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "D3";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double weight = ctx.getWeightOrDefault(70.0);
        int age = ctx.getAgeOrDefault(30);
        double latitude = MvpConstants.LATITUDE;

        // Группа 2: силовая → SS + TSS, кардио → только TSS
        double ss = 0.0;
        double tss = ctx.getTssOrZero();
        if (ctx.getWorkoutType() == WorkoutType.STRENGTH) {
            ss = ctx.getSsOrZero();
        }

        // Модификаторы
        double ageBonus = (age > 50) ? 1000.0 : 0.0;
        double latitudeBonus = (latitude > 40) ? 2000.0 : 0.0;

        double value = (weight * 20.0)
                + ageBonus
                + latitudeBonus
                + ((ss + tss) * 10.0);

        // Дополнительный расход
        value += MvpConstants.EXTRA_D3;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "D3 = (вес × 20) + age_bonus + latitude_bonus + ((SS + TSS) × 10) + 200";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double weight = ctx.getWeightOrDefault(70.0);
        int age = ctx.getAgeOrDefault(30);
        double latitude = MvpConstants.LATITUDE;

        double ss = ctx.getWorkoutType() == WorkoutType.STRENGTH ? ctx.getSsOrZero() : 0.0;
        double tss = ctx.getTssOrZero();

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("weight", weight);
        vars.put("age", age);
        vars.put("age_bonus", age > 50 ? 1000.0 : 0.0);
        vars.put("latitude", latitude);
        vars.put("latitude_bonus", latitude > 40 ? 2000.0 : 0.0);
        vars.put("SS", ss);
        vars.put("TSS", tss);
        vars.put("extra", MvpConstants.EXTRA_D3);
        return vars;
    }
}
