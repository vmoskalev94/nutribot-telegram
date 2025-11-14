package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин B6
 * <p>
 * Формула из спецификации:
 * B6 = 0.02 * protein_intake + 0.5 + TSS_total * 0.15
 * <p>
 * MVP:
 * - protein_intake = 1.5 г на 1 кг веса.
 */
@Component
public class VitaminB6Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "B6";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        double proteinIntake = weight * 1.5; // г/сутки

        double value = 0.02 * proteinIntake
                + 0.5
                + tss * 0.15;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "B6 = (protein_intake_g * 0.02) + 0.5 + (tss_total * 0.15)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        double proteinIntake = weight * 1.5;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("protein_intake_g", proteinIntake);
        vars.put("tss_total", tss);
        return vars;
    }

    private double orDefault(Number n, double def) {
        return n == null ? def : n.doubleValue();
    }
}
