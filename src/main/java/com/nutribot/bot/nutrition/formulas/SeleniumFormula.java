package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Селен (Se)
 * <p>
 * Формула из спецификации:
 * SE = (thyroid_mass * 0.8)
 * + (heavy_metal_exposure * 15)
 * + TSS_total * 0.5
 * <p>
 * MVP:
 * - thyroid_mass = 20 г для мужчин, 18 г для женщин.
 * - heavy_metal_exposure = 1, если не курит; 2, если курит.
 */
@Component
public class SeleniumFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "SE";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        String sex = ctx.getSex();
        double tss = orDefault(ctx.getTssTotal(), 0.0);
        double smokePacks = orDefault(ctx.getSmokePacksPerDay(), 0.0);

        boolean male = isMale(sex);
        boolean female = isFemale(sex);
        if (!male && !female) {
            male = true;
        }

        double thyroidMass = male ? 20.0 : 18.0;

        double heavyMetalExposure = smokePacks > 0.0 ? 2.0 : 1.0;

        double value = thyroidMass * 0.8
                + heavyMetalExposure * 15.0
                + tss * 0.5;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "SE = (thyroid_mass_g * 0.8) + (heavy_metal_exposure * 15) + (tss_total * 0.5)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        String sex = ctx.getSex();
        double tss = orDefault(ctx.getTssTotal(), 0.0);
        double smokePacks = orDefault(ctx.getSmokePacksPerDay(), 0.0);

        boolean male = isMale(sex);
        boolean female = isFemale(sex);
        if (!male && !female) {
            male = true;
        }

        double thyroidMass = male ? 20.0 : 18.0;
        double heavyMetalExposure = smokePacks > 0.0 ? 2.0 : 1.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("thyroid_mass_g", thyroidMass);
        vars.put("heavy_metal_exposure", heavyMetalExposure);
        vars.put("tss_total", tss);
        return vars;
    }

    private double orDefault(Number n, double def) {
        return n == null ? def : n.doubleValue();
    }

    private boolean isMale(String sex) {
        if (sex == null) return false;
        String s = sex.trim().toUpperCase();
        return s.startsWith("M") || s.startsWith("М");
    }

    private boolean isFemale(String sex) {
        if (sex == null) return false;
        String s = sex.trim().toUpperCase();
        return s.startsWith("F") || s.startsWith("Ж");
    }
}
