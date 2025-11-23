package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Омега-3 (EPA + DHA)
 * <p>
 * Формула из спецификации:
 * OMEGA3 = (brain_weight * 0.5)
 * + (inflammation_level * 100)
 * + TSS_total * 45
 * <p>
 * MVP:
 * - brain_weight = 1000 + (0.7 * weight) + (0.5 * height) - (0.4 * age)
 * - inflammation_level = 1
 */
@Component
public class Omega3Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "OMEGA3";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        int height = orDefaultInt(ctx.getHeightCm(), 175);
        int age = orDefaultInt(ctx.getAgeYears(), 30);
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        double brainWeight = 1000.0
                + 0.7 * weight
                + 0.5 * height
                - 0.4 * age;

        if (brainWeight < 0) {
            brainWeight = 0.0;
        }

        double inflammationLevel = 1.0;

        double value = 300 + brainWeight * 0.5
                + inflammationLevel * 100.0
                + tss * 45.0;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "OMEGA3 = 300 + (brain_weight_g * 0.5) + (inflammation_level * 100) + (tss_total * 45)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        int height = orDefaultInt(ctx.getHeightCm(), 175);
        int age = orDefaultInt(ctx.getAgeYears(), 30);
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        double brainWeight = 1000.0
                + 0.7 * weight
                + 0.5 * height
                - 0.4 * age;
        if (brainWeight < 0) {
            brainWeight = 0.0;
        }

        double inflammationLevel = 1.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("brain_weight_g", brainWeight);
        vars.put("inflammation_level", inflammationLevel);
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
