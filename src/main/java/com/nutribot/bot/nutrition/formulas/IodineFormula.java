package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Йод (I)
 * <p>
 * Формула из спецификации:
 * I = (thyroid_hormones * 25)
 * + (goiter_risk * 50)
 * + TSS_total * 0.4
 * <p>
 * MVP:
 * - thyroid_hormones = 1
 * - goiter_risk: мужчины 0, женщины 1.
 */
@Component
public class IodineFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "I";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        String sex = ctx.getSex();
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        double thyroidHormones = 1.0;

        boolean female = isFemale(sex);
        int goiterRisk = female ? 1 : 0;

        double value = thyroidHormones * 25.0
                + goiterRisk * 50.0
                + tss * 0.4;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "I = (thyroid_hormones * 25) + (goiter_risk * 50) + (tss_total * 0.4)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        String sex = ctx.getSex();
        double tss = orDefault(ctx.getTssTotal(), 0.0);

        double thyroidHormones = 1.0;
        boolean female = isFemale(sex);
        int goiterRisk = female ? 1 : 0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("thyroid_hormones", thyroidHormones);
        vars.put("goiter_risk", goiterRisk);
        vars.put("tss_total", tss);
        return vars;
    }

    private double orDefault(Number n, double def) {
        return n == null ? def : n.doubleValue();
    }

    private boolean isFemale(String sex) {
        if (sex == null) return false;
        String s = sex.trim().toUpperCase();
        return s.startsWith("F") || s.startsWith("Ж");
    }
}
