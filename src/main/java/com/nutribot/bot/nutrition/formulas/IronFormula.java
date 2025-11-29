package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Железо (Fe)
 * <p>
 * Формула (PDF):
 * Fe = clamp(8 + TSS×0.005 + CRP×0.2 + EXTRA,
 *            MIN=6, MAX=20)
 * <p>
 * Единицы: мг
 * Потери при аэробной нагрузке. Не использовать SS.
 */
@Component
public class IronFormula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "FE";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double crp = MvpConstants.CRP;
        double tss = ctx.getTssOrZero();

        // Базовая формула по PDF
        double value = 8.0
                + (tss * 0.005)
                + (crp * 0.2);

        // Дополнительный расход
        value += MvpConstants.EXTRA_FE;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.FE_MIN, NutrientLimits.FE_MAX);
    }

    @Override
    public String template() {
        return "Fe = clamp(8 + TSS×0.005 + CRP×0.2 + extra, 6, 20)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double crp = MvpConstants.CRP;
        double tss = ctx.getTssOrZero();

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("TSS", tss);
        vars.put("CRP", crp);
        vars.put("extra", MvpConstants.EXTRA_FE);
        vars.put("min", NutrientLimits.FE_MIN);
        vars.put("max", NutrientLimits.FE_MAX);
        return vars;
    }
}
