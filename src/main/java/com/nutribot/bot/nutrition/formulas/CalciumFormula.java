package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Кальций (Ca)
 * <p>
 * Формула (PDF):
 * Ca = clamp(1000 + bone_mass×10 + TSS×0.1 + EXTRA,
 *            MIN=800, MAX=2500)
 * <p>
 * Единицы: мг
 * Синергия с D3 и K2. Избыток — риск камней.
 */
@Component
public class CalciumFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "CA";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double boneMass = ctx.getBoneMassOrDefault(3.0);
        double tss = ctx.getTssOrZero();

        // Базовая формула по PDF
        double value = 1000.0
                + (boneMass * 10.0)
                + (tss * 0.1);

        // Дополнительный расход
        value += MvpConstants.EXTRA_CA;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.CA_MIN, NutrientLimits.CA_MAX);
    }

    @Override
    public String template() {
        return "Ca = clamp(1000 + bone_mass×10 + TSS×0.1 + extra, 800, 2500)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double boneMass = ctx.getBoneMassOrDefault(3.0);
        double tss = ctx.getTssOrZero();

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("bone_mass", boneMass);
        vars.put("TSS", tss);
        vars.put("extra", MvpConstants.EXTRA_CA);
        vars.put("min", NutrientLimits.CA_MIN);
        vars.put("max", NutrientLimits.CA_MAX);
        return vars;
    }
}
