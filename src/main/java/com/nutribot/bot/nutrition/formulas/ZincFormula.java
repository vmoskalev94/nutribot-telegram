package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Цинк (Zn)
 * <p>
 * Формула из спецификации:
 * ZN = (lean_mass * 0.3) + (sweat_rate * 0.2) + TSS_total * 0.7
 * <p>
 * MVP:
 * - lean_mass считается по полу:
 * Женщины: lean_mass = 0.29569 * weight + 0.41813 * height - 43.2933
 * Мужчины: lean_mass = 0.32810 * weight + 0.33929 * height - 29.5336
 * - sweat_rate ~ (base_sweat * 0.2),
 * base_sweat = 1.0 для мужчин, 0.75 для женщин.
 */
@Component
public class ZincFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "ZN";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        int height = orDefaultInt(ctx.getHeightCm(), 175);
        double tss = orDefault(ctx.getTssTotal(), 0.0);
        String sex = ctx.getSex();

        boolean male = isMale(sex);
        boolean female = isFemale(sex);
        if (!male && !female) {
            male = true; // дефолт: считаем мужские коэффициенты
        }

        double leanMass;
        if (female) {
            leanMass = 0.29569 * weight + 0.41813 * height - 43.2933;
        } else {
            leanMass = 0.32810 * weight + 0.33929 * height - 29.5336;
        }
        if (leanMass < 0) {
            leanMass = 0.0;
        }

        double baseSweat = male ? 1.0 : (female ? 0.75 : 0.9);
        // MVP: используем только базовый множитель, без динамики по нагрузке
        double sweatRate = baseSweat * 0.2;

        double value = leanMass * 0.3
                + sweatRate * 0.2
                + tss * 0.7;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "ZN = (lean_mass_kg * 0.3) + (sweat_rate_l_per_hour * 0.2) + (tss_total * 0.7)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double weight = orDefault(ctx.getWeightKg(), 70.0);
        int height = orDefaultInt(ctx.getHeightCm(), 175);
        double tss = orDefault(ctx.getTssTotal(), 0.0);
        String sex = ctx.getSex();

        boolean male = isMale(sex);
        boolean female = isFemale(sex);
        if (!male && !female) {
            male = true;
        }

        double leanMass;
        if (female) {
            leanMass = 0.29569 * weight + 0.41813 * height - 43.2933;
        } else {
            leanMass = 0.32810 * weight + 0.33929 * height - 29.5336;
        }
        if (leanMass < 0) {
            leanMass = 0.0;
        }

        double baseSweat = male ? 1.0 : (female ? 0.75 : 0.9);
        double sweatRate = baseSweat * 0.2;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("lean_mass_kg", leanMass);
        vars.put("sweat_rate_l_per_hour", sweatRate);
        vars.put("tss_total", tss);
        return vars;
    }

    private double orDefault(Number n, double def) {
        return n == null ? def : n.doubleValue();
    }

    private int orDefaultInt(Integer n, int def) {
        return n == null ? def : n;
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
