package com.nutribot.bot.nutrition.formulas;

import com.nutribot.bot.nutrition.ExplainableNutrientFormula;
import com.nutribot.bot.nutrition.MvpConstants;
import com.nutribot.bot.nutrition.NutrientContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Витамин D3
 * <p>
 * Формула (PDF):
 * D3 = clamp(1000 + (lat > 40 ? 1000 : 0) + TSS×0.1 + (CRP > 2.0 ? 500 : 0) + EXTRA,
 *            MIN=600, MAX=4000)
 * <p>
 * Единицы: МЕ
 * Активация зависит от Mg. Синергия с K2 и Ca.
 */
@Component
public class VitaminD3Formula implements ExplainableNutrientFormula {

    @Override
    public String code() {
        return "D3";
    }

    @Override
    public double calculate(NutrientContext ctx) {
        double latitude = MvpConstants.LATITUDE;
        double crp = MvpConstants.CRP;
        double tss = ctx.getTssOrZero();

        // Базовая формула по PDF
        double value = 1000.0;

        // Модификаторы
        if (latitude > 40) {
            value += 1000.0;
        }
        value += tss * 0.1;
        if (crp > 2.0) {
            value += 500.0;
        }

        // Дополнительный расход
        value += MvpConstants.EXTRA_D3;

        // Clamp в диапазон [MIN, MAX]
        return NutrientLimits.clamp(value, NutrientLimits.D3_MIN, NutrientLimits.D3_MAX);
    }

    @Override
    public String template() {
        return "D3 = clamp(1000 + lat_bonus + TSS×0.1 + crp_bonus + extra, 600, 4000)";
    }

    @Override
    public Map<String, Object> vars(NutrientContext ctx) {
        double latitude = MvpConstants.LATITUDE;
        double crp = MvpConstants.CRP;
        double tss = ctx.getTssOrZero();

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("latitude", latitude);
        vars.put("lat_bonus", latitude > 40 ? 1000.0 : 0.0);
        vars.put("TSS", tss);
        vars.put("CRP", crp);
        vars.put("crp_bonus", crp > 2.0 ? 500.0 : 0.0);
        vars.put("extra", MvpConstants.EXTRA_D3);
        vars.put("min", NutrientLimits.D3_MIN);
        vars.put("max", NutrientLimits.D3_MAX);
        return vars;
    }
}
