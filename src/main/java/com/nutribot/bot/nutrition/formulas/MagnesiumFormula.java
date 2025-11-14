package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Магний (Mg)
 * <p>
 * Формула из спецификации:
 * MG = 300
 * + (weight * 1.5)
 * + (height - 175) * 0.2
 * + TSS_total * 12
 * <p>
 * 12 мг/TSS — стресс-коррекция по суммарному TSS.
 */
@Component
public class MagnesiumFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "MG";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        int height = orDefaultInt(ctx.getHeightCm(), 175);
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        double value = 300.0
                + weight * 1.5
                + (height - 175.0) * 0.2
                + tss * 12.0;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "MG = 300 + (weight_kg * 1.5) + (height_cm_minus_175 * 0.2) + (tss_total * 12)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        int height = orDefaultInt(ctx.getHeightCm(), 175);
        double tss = orDefault(ctx.getTssTotal(), 0.0);
        double heightDelta = height - 175.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("weight_kg", weight);
        vars.put("height_cm_minus_175", heightDelta);
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
