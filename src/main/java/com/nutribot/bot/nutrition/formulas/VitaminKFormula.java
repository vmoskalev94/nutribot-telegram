package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин K
 * <p>
 * Формула из спецификации:
 * K = (vascular_length * 0.0001)
 * + (warfarin_dose * 20)
 * + TSS_total * 0.05
 * <p>
 * MVP:
 * - warfarin_dose = 0
 * - vascular_length:
 * мужчины: ~210_000
 * женщины: ~190_000
 * (без поправок на эндоморф/выносливость).
 */
@Component
public class VitaminKFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "K";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        String sex = ctx.getSex();
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        boolean male = isMale(sex);
        boolean female = isFemale(sex);
        if (!male && !female) {
            male = true;
        }

        double vascularLength = male ? 210_000.0 : 190_000.0;
        double warfarinDose = 0.0;

        double value = vascularLength * 0.0001
                + warfarinDose * 20.0
                + tss * 0.05;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "K = (vascular_length * 0.0001) + (warfarin_dose * 20) + (tss_total * 0.05)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        String sex = ctx.getSex();
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        boolean male = isMale(sex);
        boolean female = isFemale(sex);
        if (!male && !female) {
            male = true;
        }

        double vascularLength = male ? 210_000.0 : 190_000.0;
        double warfarinDose = 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("vascular_length", vascularLength);
        vars.put("warfarin_dose", warfarinDose);
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
