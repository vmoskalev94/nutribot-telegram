package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин E
 * <p>
 * Формула из спецификации:
 * E = (cell_membrane_area * 0.002)
 * + (radiation_exposure * 10)
 * + TSS_total * 0.6
 * <p>
 * MVP:
 * - cell_membrane_area:
 * мужчины: 115
 * женщины: 100
 * - radiation_exposure = 0
 */
@Component
public class VitaminEFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "E";
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

        double cellMembraneArea = male ? 115.0 : 100.0;
        double radiationExposure = 0.0;

        double value = cellMembraneArea * 0.002
                + radiationExposure * 10.0
                + tss * 0.6;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "E = (cell_membrane_area * 0.002) + (radiation_exposure * 10) + (tss_total * 0.6)";
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

        double cellMembraneArea = male ? 115.0 : 100.0;
        double radiationExposure = 0.0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("cell_membrane_area", cellMembraneArea);
        vars.put("radiation_exposure", radiationExposure);
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
