package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Железо (Fe)
 * <p>
 * Формула из спецификации:
 * FE = (weight * 0.15) + (blood_donation * 50) + TSS_total * 0.8
 * <p>
 * MVP:
 * - blood_donation = 0 (нет учёта донаций за 90 дней).
 */
@Component
public class IronFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "FE";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        double tss = orDefault(ctx.getTssTotal(), 0.0);
        double bloodDonation = 0.0;

        double value = weight * 0.15
                + bloodDonation * 50.0
                + tss * 0.8;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "FE = (weight_kg * 0.15) + (blood_donation_units * 50) + (tss_total * 0.8)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        double tss = orDefault(ctx.getTssTotal(), 0.0);
        double bloodDonation = 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("weight_kg", weight);
        vars.put("blood_donation_units", bloodDonation);
        vars.put("tss_total", tss);
        return vars;
    }

    private double orDefault(Number n, double def) {
        return n == null ? def : n.doubleValue();
    }
}
