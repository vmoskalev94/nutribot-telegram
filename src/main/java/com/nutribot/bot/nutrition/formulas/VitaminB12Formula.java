package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин B12
 * <p>
 * Формула из спецификации:
 * B12 = (stomach_ph > 4 ? 2.4 : 5.0)
 * + (vegan_years * 0.5)
 * + TSS_total * 0.1
 * <p>
 * MVP:
 * - stomach_ph = 1.8 → всегда берём 5.0
 * - vegan_years = 0
 */
@Component
public class VitaminB12Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "B12";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        double stomachPh = 1.8;
        double veganYears = 0.0;

        double stomachTerm = stomachPh > 4.0 ? 2.4 : 5.0;

        double value = stomachTerm
                + veganYears * 0.5
                + tss * 0.1;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "B12 = stomach_term + (vegan_years * 0.5) + (tss_total * 0.1)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double tss = orDefault(ctx.getTssTotal(), 0.0);
        double stomachPh = 1.8;
        double veganYears = 0.0;
        double stomachTerm = stomachPh > 4.0 ? 2.4 : 5.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("stomach_term", stomachTerm);
        vars.put("stomach_ph", stomachPh);
        vars.put("vegan_years", veganYears);
        vars.put("tss_total", tss);
        return vars;
    }

    private double orDefault(Number n, double def) {
        return n == null ? def : n.doubleValue();
    }
}
