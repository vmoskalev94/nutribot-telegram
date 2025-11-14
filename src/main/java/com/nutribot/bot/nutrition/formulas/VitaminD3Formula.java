package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин D3
 * <p>
 * Формула из спецификации:
 * D3 = (weight * 15)
 * + (age > 50 ? 400 : 0)
 * + (sun_exposure < 15 ? 800 : 0)
 * + TSS_total * 15
 * <p>
 * 15 МЕ/TSS — поправка на нагрузку.
 */
@Component
public class VitaminD3Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "D3";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        int age = orDefaultInt(ctx.getAgeYears(), 30);
        double sun = orDefault(ctx.getSunExposureMinutes(), 0.0);
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        double ageBonus = age > 50 ? 400.0 : 0.0;
        double sunBonus = sun < 15.0 ? 800.0 : 0.0;

        double value = weight * 15.0
                + ageBonus
                + sunBonus
                + tss * 15.0;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "D3 = (weight_kg * 15) + age_bonus + sun_bonus + (tss_total * 15)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        int age = orDefaultInt(ctx.getAgeYears(), 30);
        double sun = orDefault(ctx.getSunExposureMinutes(), 0.0);
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        double ageBonus = age > 50 ? 400.0 : 0.0;
        double sunBonus = sun < 15.0 ? 800.0 : 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("weight_kg", weight);
        vars.put("age_bonus", ageBonus);
        vars.put("sun_bonus", sunBonus);
        vars.put("tss_total", tss);
        return vars;
    }

    private double orDefault(Number n, double def) {
        return n == null ? def : n.doubleValue();
    }

    private int orDefaultInt(Integer n, int def) {
        return n == null ? def : n;
    }
}
