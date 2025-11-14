package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин C
 * <p>
 * Формула из спецификации:
 * C = (weight * 1.2)
 * + (smoke_packs * 35)
 * + (pollution_level * 20)
 * + TSS_total * 8
 * <p>
 * MVP:
 * - smoke_packs берём из контекста (packs/день).
 * - pollution_level = 37 (для Москвы, пока константа).
 */
@Component
public class VitaminCFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "C";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        double smokePacks = orDefault(ctx.getSmokePacksPerDay(), 0.0);
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        double pollutionLevel = 37.0; // MVP: константа

        double value = weight * 1.2
                + smokePacks * 35.0
                + pollutionLevel * 20.0
                + tss * 8.0;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "C = (weight_kg * 1.2) + (smoke_packs * 35) + (pollution_level * 20) + (tss_total * 8)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        double smokePacks = orDefault(ctx.getSmokePacksPerDay(), 0.0);
        double tss = orDefault(ctx.getTssTotal(), 0.0);
        double pollutionLevel = 37.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("weight_kg", weight);
        vars.put("smoke_packs", smokePacks);
        vars.put("pollution_level", pollutionLevel);
        vars.put("tss_total", tss);
        return vars;
    }

    private double orDefault(Number n, double def) {
        return n == null ? def : n.doubleValue();
    }
}
