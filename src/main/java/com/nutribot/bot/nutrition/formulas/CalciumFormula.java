package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Кальций (Ca)
 * <p>
 * Формула из спецификации:
 * CA = 5 * bone_mass + (pregnancy * 300) + TSS_total * 10
 * <p>
 * MVP:
 * - bone_mass:
 * мужчины:  bone_mass = 0.21 * weight + 0.18 * height - 12.2
 * женщины:  bone_mass = 0.14 * weight + 0.22 * height - 5.4
 * - pregnancy убрать на MVP → pregnancy_factor = 0 даже если флаг беременна true.
 */
@Component
public class CalciumFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "CA";
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
            male = true;
        }

        double boneMass;
        if (male) {
            boneMass = 0.21 * weight + 0.18 * height - 12.2;
        } else {
            boneMass = 0.14 * weight + 0.22 * height - 5.4;
        }
        if (boneMass < 0) {
            boneMass = 0.0;
        }

        int pregnancyFactor = 0; // MVP: беременность не учитываем

        double value = boneMass * 5.0
                + pregnancyFactor * 300.0
                + tss * 10.0;

        return Math.max(value, 0.0);
    }

    @Override
    public String template() {
        return "CA = (bone_mass_kg * 5) + (pregnancy_factor * 300) + (tss_total * 10)";
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

        double boneMass;
        if (male) {
            boneMass = 0.21 * weight + 0.18 * height - 12.2;
        } else {
            boneMass = 0.14 * weight + 0.22 * height - 5.4;
        }
        if (boneMass < 0) {
            boneMass = 0.0;
        }

        int pregnancyFactor = 0;

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("bone_mass_kg", boneMass);
        vars.put("pregnancy_factor", pregnancyFactor);
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
